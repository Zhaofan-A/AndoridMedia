package com.byd.videoSdk.recover.mdat;

import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;


import com.byd.videoSdk.recorder.file.VideoMpeg4File;
import com.byd.videoSdk.recorder.utils.SDKMediaFormat;
import com.byd.videoSdk.common.util.BYDLog;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Mp4Reumser {
    private static final String TAG = "Mp4Reumser";
    private static final int MAX_SIZE = 1024 * 1024;
    private boolean mIsResume;

    public boolean stopResume(){
        mIsResume = false;
        return true;
    }
    /**
     * Resume from bad mp4.
     * @param srcMp4
     * @param mp4Index
     * @param dstMp4
     * @return
     * @throws IOException
     */
    public boolean resumeFrombadMp4(String srcMp4, String mp4Index, String dstMp4) throws IOException{
        BYDLog.d(TAG, "resumeFrombadMp4: mp4 filename : (srcMp4) " + srcMp4);
        BYDLog.d(TAG, "resumeFrombadMp4: mp4 filename : mp4IndexFile " + mp4Index);
        BYDLog.d(TAG, "resumeFrombadMp4: mp4 filename : dstMp4 " + dstMp4);

        if(srcMp4 == null || !(new File(srcMp4).exists())){
            BYDLog.d(TAG, "resumeFrombadMp4: srcMp4 not exist");
            return false;
        }
        if(mp4Index == null || !(new File(mp4Index).exists()) || new File(mp4Index).length() < 20){//One frame index data is 20bytes
            BYDLog.d(TAG, "resumeFrombadMp4: mp4IndexFile not exist");
            return false;
        }
        if(dstMp4 == null || (new File(dstMp4).exists())){//Is new file, so here not exists
            BYDLog.d(TAG, "resumeFrombadMp4: dstMp4 is null or exist");
            return false;
        }

        mIsResume = true;
        boolean isOK = false;
        DataInputStream dIn = null;
        BufferInfo bufferInfo = new BufferInfo();
        ByteBuffer buffer = ByteBuffer.allocate(MAX_SIZE);
        byte[] data = null;
        MDatFrameIndexReader reader = new MDatFrameIndexReader(mp4Index);
        VideoMpeg4File dstFile = null;
        boolean find = false;
        try{
            dIn = new DataInputStream(new FileInputStream(srcMp4));
            //Find the mdat box
            find = Mp4Extractor.findMDatBoxOffset(dIn);  //跳到mdat开始的box位置，因为这个box里面存储的才是真正的视频数据
            int readSize = 0;
            if(find){
                dstFile = new VideoMpeg4File(dstMp4);
                //Fisrt is w and h
//                int w = reader.readInt();
//                int h = reader.readInt();

               int spsLength =  reader.readInt();
               byte[] sps = new byte[spsLength];
               reader.read(sps,0,spsLength);
                int ppsLength =  reader.readInt();
                byte[] pps = new byte[ppsLength];
                reader.read(pps,0,ppsLength);

                BYDLog.d(TAG, "resumeFrombadMp4_start: "+ spsLength+"   "+ppsLength);
                BYDLog.d(TAG, "resumeFrombadMp4_start: "+ Arrays.toString(sps)+"   "+ Arrays.toString(pps));

                //Set mp4 mediaformat
                dstFile.setVideoMediaFormat(SDKMediaFormat.getMPEG4MediaFormat(sps, pps));
                while(true){
                    if(!mIsResume){
                        BYDLog.d(TAG, "resumeFrombadMp4: stop resume by user.!");
                        if(VideoMpeg4File.check(dstMp4, MediaFormat.MIMETYPE_VIDEO_AVC)
                                && VideoMpeg4File.getLength(dstMp4) > VideoMpeg4File.getLength(srcMp4)-1024*1024*2){
                            isOK = true;
                        }else{
                            isOK = false;
                        }
                        break;
                    }
                    //Read buffer info of every frame from index file.
                    boolean ret = reader.read(bufferInfo);
                    BYDLog.v(TAG, "resumeFrombadMp4: ret " + ret);
                    if(ret == false){//Maybe err or end
                        break;
                    }
                    BYDLog.v(TAG, "resumeFrombadMp4: data " + data);
                    BYDLog.v(TAG, "resumeFrombadMp4: bufferInfo.size " + bufferInfo.size);
                    BYDLog.v(TAG, "resumeFrombadMp4: bufferInfo.flags " + bufferInfo.flags);
                    BYDLog.v(TAG, "resumeFrombadMp4: bufferInfo.presentationTimeUs " + bufferInfo.presentationTimeUs);
                    BYDLog.v(TAG, "resumeFrombadMp4: bufferInfo.offset " + bufferInfo.offset);
                    if (bufferInfo.size > MAX_SIZE) {
                        BYDLog.e(TAG, "resumeFrombadMp4: bufferInfo.size greater than max size  " + bufferInfo.size);
                        break;
                    }
                    //If buffer is smaller, new buffer.
                    if(data == null || data.length < bufferInfo.size){
                        data = new byte[bufferInfo.size];
                    }
                    BYDLog.v(TAG, "resumeFrombadMp4: byte buffer length " + data.length);
                    //The mdat box framedata length.
                    /*
                     * unsigned int len;
                     * unsigned char name[4];
                     * sample0{
                     *     unsigned int sampleSize;//sample length that not include sampleSize
                     *     unsigned int sample[sampleSize];//H264 data
                     * }
                     * sample_1
                     *   ...
                     * sample_n
                     */
                    try{
                        //MP4文件中的所有数据都装在box（QuickTime中为atom）中，也就是说MP4文件由若干个box组成，每个box有类型和长度
                        // 标准的box开头的4个字节（32位）为box size，该大小包括box header和box body整个box的大小,size==0时，代表这是文件中最后一个Box；当size==1时，意味着Box长度需要更多bits来描述
                        readSize = dIn.readInt();    //  此处的readSize 表示mdat媒体数据中 码流单个nalu的长度

                        if(readSize <= 0) {
                            BYDLog.e(TAG, "resumeFrombadMp4: dIn readInt size " + readSize);
                            break;
                        }
                        if(bufferInfo.size != readSize + 4){   //TODO          4指的是00 00 00 01长度
                            BYDLog.w(TAG, "resumeFrombadMp4: readSize " + readSize);
                            break;
                        }
                        BYDLog.v(TAG, "resumeFrombadMp4: the mdat frame size " + readSize);
                        readSize = dIn.read(data, 0, readSize/*bufferInfo.size*/);   //从数组的offset位置开始，并且最多将length个字节写入到数组中
                        BYDLog.v(TAG, "resumeFrombadMp4: read data size " + readSize);
                        if(readSize <= 0){
                            BYDLog.e(TAG, "resumeFrombadMp4: dIn read size " + readSize);
                            break;
                        }
                        if (readSize > MAX_SIZE) {
                            BYDLog.e(TAG, "resumeFrombadMp4: dIn read size greater than max size  " + readSize);
                            break;
                        }
                    } catch(EOFException e){
                        BYDLog.w(TAG, "resumeFrombadMp4: bad mp4 eof ");
                        e.printStackTrace();
                        break;
                    } catch(Exception e){
                        BYDLog.w(TAG, "resumeFrombadMp4: failed ");
                        e.printStackTrace();
                        break;
                    }
                    buffer.clear();
                    buffer.put(data);
                    if(!dstFile.writeSampeData(buffer, bufferInfo)){
                        BYDLog.v(TAG, "resumeFrombadMp4: write sample data fail");
                        break;
                    }
                    BYDLog.v(TAG, "resumeFrombadMp4: will continue...");
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            BYDLog.d(TAG, "resumeFrombadMp4: finally!");
            try{
                if(dIn != null){
                    dIn.close();
                    dIn = null;
                }
                if(dstFile != null){
                    dstFile.close();
                    dstFile = null;
                }
                if(reader != null){
                    reader.closeFile();
                    reader = null;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        if(!mIsResume){
            BYDLog.d(TAG, "resumeFrombadMp4: X, stop resume by user.!");
            if(isOK){
                return true;
            }
            return false;
        }
        mIsResume = false;
        BYDLog.d(TAG, "resumeFrombadMp4: X");
        return find;
    }
}
