package com.byd.videoSdk.recover.mdat;

import android.annotation.SuppressLint;
import android.media.MediaCodec.BufferInfo;


import com.byd.videoSdk.recover.buffer.VectorBuffer;
import com.byd.videoSdk.recorder.file.VideoJavaFile;
import com.byd.videoSdk.common.util.BYDLog;

import java.io.IOException;

/**
 * Writer for save h264 frame from codec.
 *
 * @author bai.yu1
 */
@SuppressLint("NewApi")
public class MDatFrameIndexWriter {
    private static final String TAG = "MDatFrameIndexWriter";
    //If rws/rwd open file, must use data buffer.
    private static boolean isRWS = false;
    private VideoJavaFile mFile = null;//File in java.

    public MDatFrameIndexWriter(String name) throws IOException {
        super();
        if (!openFile(name)) {
            throw new IOException("The file " + name + " open fail");
        }
        //For write buffer. And sync to write to file.
    }

    public MDatFrameIndexWriter(String name, boolean rws) throws IOException {
        super();
        isRWS = rws;
        if (!openFile(name)) {
            throw new IOException("The file " + name + " open fail");
        }
        //For write buffer. And sync to write to file.
    }
    /**
     * Open the file.
     * Must open then can write.
     *
     * @param name
     * @return
     */
    private synchronized boolean openFile(String name) {
        String mode = "rw";
        if (isRWS) {
            mode = "rwd";//d is O_DIRECT
        }
        mFile = new VideoJavaFile();
        if (mFile.open(name, mode)) {
            return true;
        }
        return false;
    }

    /**
     * Close the file.
     * If close, the file can not be writed.
     */
    public synchronized void closeFile() {
        BYDLog.d(TAG, "closeFile: E");
        if(mFile != null){
            if (isRWS) {
                mFile.sync();
            }
            mFile.close();
        }
        BYDLog.d(TAG, "closeFile: X");
    }

    /**
     * Write the data to file to opend.
     *
     * @param bufferInfo
     * @return
     */
    public boolean writeBuffer(BufferInfo bufferInfo) {
        return writeToFile(bufferInfo);
    }
    public boolean writeInt(int value){
        return writeToFile(value);
    }


    public boolean sync(){
        BYDLog.v(TAG, "sync: E");
        if(mFile != null){
            mFile.sync();
            BYDLog.v(TAG, "sync: X ");
            return true;
        }
        BYDLog.d(TAG, "sync: X err, file is null");
        return false;
    }
    /**
     * Is rws mode open random access file.
     *
     * @return
     */
    public synchronized boolean isRWS() {
        return isRWS;
    }
    private synchronized boolean writeToFile(int value) {
        BYDLog.v(TAG, "writeToFile: E.");
        if (mFile == null) {
            BYDLog.e(TAG, "writeToFile: X, file is not opend.");
            return false;
        }
        try {
            if (mFile != null) {
                BYDLog.v(TAG, "writeToFile: will write int to file[" + mFile.getName() + "].");
                mFile.writeInt(value);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
           // Toast.makeText(SentryModeApplication.getContext(), "write file " + e, Toast.LENGTH_LONG).show();
        }
        BYDLog.w(TAG, "writeToFile: X, err");
        return false;
    }
    /**
     * Write data to file.
     *
     * @param bufferInfo
     * @return
     */
    private synchronized boolean writeToFile(BufferInfo bufferInfo) {
        BYDLog.d(TAG, "writeToFile: E.");
        if (mFile == null) {
            BYDLog.e(TAG, "writeToFile: X, file is not opend.");
            return false;
        }
        if (bufferInfo == null) {
            BYDLog.e(TAG, "writeToFile: X, err input is null.");
            return false;
        }
        //Check bufferInfo and buffer
        BYDLog.v(TAG, "writeToFile: bufferInfo.size is " + bufferInfo.size);
        BYDLog.v(TAG, "writeToFile: bufferInfo.offset is " + bufferInfo.offset);
        BYDLog.v(TAG, "writeToFile: bufferInfo.presentationTimeUs is " + bufferInfo.presentationTimeUs);
        BYDLog.v(TAG, "writeToFile: bufferInfo.flags is " + bufferInfo.flags);
        if (bufferInfo.size < 0 || bufferInfo.offset < 0
                || bufferInfo.presentationTimeUs < 0) {
            BYDLog.e(TAG, "writeToFile: X, err bufferInfo must specify a" +
                    " valid buffer offset, size and presentation time");
            return false;
        }

        try {
            if (mFile != null) {
                BYDLog.v(TAG, "writeToFile: will write data to file[" + mFile.getName() + "].");
                mFile.writeInt(bufferInfo.offset);
                mFile.writeInt(bufferInfo.size);
                mFile.writeLong(bufferInfo.presentationTimeUs);
                mFile.writeInt(bufferInfo.flags);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
           // Toast.makeText(SentryModeApplication.getContext(), "write file " + e, Toast.LENGTH_LONG).show();
        }
        BYDLog.w(TAG, "writeToFile: X, err");
        return false;
    }

    public synchronized boolean writeToFile(byte [] bytes) {
        BYDLog.v(TAG, "writeToFile: bytes E.");
        if (mFile == null) {
            BYDLog.e(TAG, "writeToFile: X, file is not opend.");
            return false;
        }
        if (bytes == null) {
            BYDLog.e(TAG, "writeToFile: X, err input is null.");
            return false;
        }
        if (bytes.length == 0 ) {
            BYDLog.e(TAG, "writeToFile: X, err bufferInfo must specify a" +
                    " valid buffer offset, size and presentation time");
            return false;
        }

        try {
            if (mFile != null) {
                BYDLog.d(TAG, "writeToFile: will write data to file[" + mFile.getName() + "].");
                mFile.write(bytes,0,bytes.length);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
           // Toast.makeText(SentryModeApplication.getContext(), "write file " + e, Toast.LENGTH_LONG).show();
        }
        BYDLog.w(TAG, "writeToFile: bytes X, err");
        return false;
    }


    /**
     * Write the file
     *
     * @param buf
     * @return
     */
    private synchronized void writeToFile(VectorBuffer buf) {
        boolean ret = writeBufferToFile(buf);
    }

    /**
     * Write the buf to file
     *
     * @param buf
     * @return
     */
    private synchronized boolean writeBufferToFile(VectorBuffer buf) {
        BYDLog.v(TAG, "writeToFile: E.");
        if (mFile == null) {
            BYDLog.e(TAG, "writeToFile: X, file is not opend.");
            return false;
        }
        if (buf == null) {
            BYDLog.e(TAG, "writeToFile: X, err input is null.");
            return false;
        }
        //Check bufferInfo and buffer
        BYDLog.v(TAG, "writeToFile: data size is " + buf.getValidLen());
        if (buf.getValidLen() > 64) {
            try {
                byte[] bData = buf.get();
                BYDLog.v(TAG, "writeToFile: data len is " + bData.length);
                if (bData != null) {
                    int ret = -1;
                    if (mFile != null) {
                        ret = mFile.write(bData, 0, buf.getValidLen());
                        if(isRWS){
                            mFile.sync();
                        }
                    }

                    if (ret == -1) {
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
             //   Toast.makeText(SentryModeApplication.getContext(), "write file " + e, Toast.LENGTH_LONG).show();
                return false;
            }
        }
        BYDLog.v(TAG, "writeToFile: X");
        return true;
    }

}
