package com.byd.videoSdk.recover;

import android.media.MediaFormat;
import android.text.TextUtils;

import com.byd.videoSdk.common.handler.AutoVideoHandler;
import com.byd.videoSdk.common.handler.SentryModeHandlerMsg;
import com.byd.videoSdk.recorder.RecorderController;
import com.byd.videoSdk.recorder.file.VideoMpeg4File;
import com.byd.videoSdk.recover.mdat.Mp4Reumser;
import com.byd.videoSdk.common.util.BYDLog;
import com.byd.videoSdk.recorder.utils.FileSwapHelper;
import com.byd.videoSdk.common.util.SharedPreferencesUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * For resume from frame data to mp4 files.
 *
 * @author bai.yu1
 */
public class MP4IndexResumeCtrlImpl extends Mp4ResumeController {

    private static final String TAG = "MP4IndexResumeCtrlImpl";
    private String PATH_COPY;
    private static final String SUFFIX_MP4 = FileSwapHelper.BASE_EXT;
    private static final String SUFFIX_INDEX = FileSwapHelper.MP4_INDEX_EXT;
    private static final String SUFFIX_MP4_CPY = FileSwapHelper.MP4_CPY_EXT;
    private boolean mIsProcessResume;
    private Mp4Reumser mFileMp4Reumser;
    private Mp4Reumser mDirMp4Reumser;

    public MP4IndexResumeCtrlImpl() {
        super();

//        String customPath = SharedPreferencesUtils.getInstance(RecorderController.getInstance().getContext(),
//                FileSwapHelper.SENTRYMODE_SDK_RECORDER_INFO).getString(FileSwapHelper.CUSTOM_VIDEO_SAVE_PATH, FileSwapHelper.getVideoPath(FileSwapHelper.ROOT));
//
//        PATH_COPY = customPath + FileSwapHelper.COPY +  File.separator;
        PATH_COPY = FileSwapHelper.getVideoPath(FileSwapHelper.COPY);

        mFileMp4Reumser = new Mp4Reumser();
        mDirMp4Reumser = new Mp4Reumser();

        BYDLog.d(TAG, "MP4IndexResumeCtrlImpl: PATH_COPY is " + PATH_COPY);
    }

    @Override
    protected boolean stopResume() {
        // TODO Auto-generated method stub
        mIsProcessResume = false;
        if (mDirMp4Reumser != null) {
            mDirMp4Reumser.stopResume();
        }
        if (mFileMp4Reumser != null) {
            mFileMp4Reumser.stopResume();
        }
        return false;
    }

    @Override
    protected ArrayList<String> processResume() {
        // TODO Auto-generated method stub
        mIsProcessResume = true;
        return handleResumeDir();
    }

    @Override
    protected String processStop(String name) {
        // TODO Auto-generated method stub
        return handleStop(name);
    }

    /**
     * copyFileExist
     *
     * @param name
     * @return
     */
    @Override
    public boolean copyFileExist(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        String indexName = this.covertMpeg4ToIndexFile(name);
        if (indexName == null) {
            return false;
        }

        return new File(indexName).exists();
    }

    /**
     * Handle stop recorder
     *
     * @param name the recorder mp4 file.
     * @return
     */
    private String handleStop(String name) {
        BYDLog.d(TAG, "handleStop: E, file name is " + name);
        if (name == null || name.isEmpty()) {
            BYDLog.e(TAG, "handleStop: current file name is " + name);
            return null;
        }
        String cpyName = this.covertMpeg4ToCpyFile(name);
        String indexName = this.covertMpeg4ToIndexFile(name);

        BYDLog.d(TAG, "handleStop: cpyName is " + cpyName);
        BYDLog.d(TAG, "handleStop: indexName is " + indexName);
        File mp4Cpy = new File(cpyName);
        //If the mp4 is good, rename the copy file to mp4.
        //FAT block is 4K; I frame length is bigger than 64K(about 100K)
        if (mp4Cpy.exists() && mp4Cpy.length() > 48
                && VideoMpeg4File.check(cpyName, MediaFormat.MIMETYPE_VIDEO_AVC)) {
            BYDLog.d(TAG, "handleStop: cpyName is good");
            if (indexName == null || indexName.isEmpty()) {
                BYDLog.e(TAG, "handleStop: MSG_RECORDER_NORMAL_STOP msg process err.");
            } else {
                try {
                    BYDLog.d(TAG, "handleStop: rename " + cpyName + " to " + name);
                    //Test
                    if (mp4Cpy.renameTo(new File(name))) {
                        File indexFile = new File(indexName);
                        if (indexFile.exists()) {
                            BYDLog.d(TAG, "handleStop: del the copy file because the mp4 is good.");
                            indexFile.delete();
                        } else {
                            BYDLog.w(TAG, "handleStop: current file is not exsits.");
                        }
                        return name;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    BYDLog.e(TAG, "", e);
                }
            }
        } else {
            BYDLog.d(TAG, "handleStop: resume ...");
            return handleResumeMp4File(cpyName, indexName, name);
        }
        return null;
    }

    /**
     * Check the copy file(.mp4.h264) and normal recorder file(.mp4).
     * If the mp4 is err, copy the .h264 file to mp4.
     */
    private String handleResumeMp4File(String cpyName, String indexName, String mpeg4Name) {
        if (mpeg4Name == null || mpeg4Name.isEmpty() || !mpeg4Name.endsWith(SUFFIX_MP4)) {
            BYDLog.e(TAG, "handleResumMp4File: mpeg4Name is " + mpeg4Name + " is err.");
            return null;
        }
        File cpyFile = new File(cpyName);
        File indexFile = new File(indexName);
        //Mp4 is good. RM index and cpy file
        if (VideoMpeg4File.check(mpeg4Name, MediaFormat.MIMETYPE_VIDEO_AVC)) { //从损坏得文件中获取MIME,
            BYDLog.w(TAG, "handleResumMp4File: mpeg4File has  found mime " + MediaFormat.MIMETYPE_VIDEO_AVC);
            BYDLog.w(TAG, "handleResumMp4File: mpeg4File is good and not copy");
            //Remove the copy file.
            BYDLog.w(TAG, "handleResumMp4File: will remove copy " + cpyName);
            cpyFile.delete();
            indexFile.delete();
            return mpeg4Name;
        }
        //Cpy file check, if good rename to mp4 and remove the index.
        if (VideoMpeg4File.check(cpyName, MediaFormat.MIMETYPE_VIDEO_AVC)) {
            BYDLog.w(TAG, "handleResumMp4File: mpeg4File has  found mime " + MediaFormat.MIMETYPE_VIDEO_AVC);
            BYDLog.w(TAG, "handleResumMp4File: mpeg4File is good and not copy");
            //Remove the copy file.
            BYDLog.w(TAG, "handleResumMp4File: will remove copy " + cpyName);
            File mp4File = new File(mpeg4Name);
            cpyFile.renameTo(mp4File);
            indexFile.delete();
            return mpeg4Name;
        }
        //Rm mp4
        File mpeg4File = new File(mpeg4Name);
        BYDLog.d(TAG, "handleResumMp4File: mp4 filename : " + (mpeg4Name) + " filesize " + mpeg4File.length());
        //Remove the bad mp4 file.
        if (VideoMpeg4File.exists(mpeg4Name)) {
            BYDLog.d(TAG, "handleResumMp4File: mp4 is exist, remove it");
            VideoMpeg4File.remove(mpeg4Name);
        }
        if (!indexFile.exists() || indexFile.length() < 20) {//20 bytes, on frame index length
            BYDLog.d(TAG, "handleResumMp4File: indexFile not exitsts or length is 0");
            indexFile.delete();
            cpyFile.delete();
            return null;
        }
        //Copy data to new mp4 file(mpeg4Name) from copy file(copyName).
        try {
            if (mFileMp4Reumser.resumeFrombadMp4(cpyName, indexName, mpeg4Name)) {
                //Remove the copy.
                BYDLog.e(TAG, "handleResumMp4File: resume [" + mpeg4Name + "] end from [" + cpyName + "]");
                //new File(copyName).delete();//Msg
                if (VideoMpeg4File.check(mpeg4Name, MediaFormat.MIMETYPE_VIDEO_AVC)) {
                    BYDLog.e(TAG, "handleResumMp4File: resume [" + mpeg4Name + "] sucessful from [" + cpyName + "]");
                    handleDelCopy(cpyName, mpeg4Name, DELETE_COPY_DELAY_TIME);
                    handleDelCopy(indexName, mpeg4Name, DELETE_COPY_DELAY_TIME);
                    return mpeg4Name;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // resume fail and rm the cpy file and the index file.
        BYDLog.e(TAG, "handleResumMp4File: resume [" + mpeg4Name + "] fail from [" + cpyName + "]");
        BYDLog.e(TAG, "handleResumMp4File: delete copy file " + cpyName);
        BYDLog.e(TAG, "handleResumMp4File: delete index file " + indexName);
        new File(cpyName).delete();
        new File(indexName).delete();
        return null;
    }

    /**
     * Check the copy file(.mp4.cpy) and normal recorder file(.mp4).
     * If the mp4 is err, copy the .cpy file to mp4.
     */
    private ArrayList<String> handleResumeDir() {
        File[] files = list(PATH_COPY);
        ArrayList<String> resumeMp4PathList = new ArrayList<String>();
        if (files == null) {
            return resumeMp4PathList;
        }

        BYDLog.d(TAG, "handleResumeDir: files size is " + files.length);
        if (files.length > 0) {
            String coverIndexCopyFile = covertIndexToCpyFile(files[0].getAbsolutePath());
            BYDLog.d(TAG, "handleResumeDir: getCurrentFile is " + getCurrentFile() + " file path " + coverIndexCopyFile);
            if (files.length == 1 && getCurrentFile() != null && coverIndexCopyFile != null && coverIndexCopyFile.contains(getCurrentFile())) {
                return resumeMp4PathList;
            }

            AutoVideoHandler.getInstance().sendMessage(SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_TFCARD_RESUME_FILE);

            for (int i = 0; i < files.length; i++) {
                String indexName = files[i].getAbsolutePath();
                if (indexName == null || !indexName.endsWith(SUFFIX_INDEX)) {
                    BYDLog.e(TAG, "handleResumeDir: index Name is " + indexName);
                    continue;
                }
                String cpyName = covertIndexToCpyFile(indexName);
                if (cpyName == null || cpyName.isEmpty() || !cpyName.endsWith(SUFFIX_MP4_CPY)) {
                    BYDLog.e(TAG, "handleResumeDir: mpeg4 cpy Name is " + cpyName + " is err.");
                    continue;
                }
                String mp4Name = covertIndexToMpeg4File(indexName);
                if (mp4Name == null || mp4Name.isEmpty() || !mp4Name.endsWith(SUFFIX_MP4)) {
                    BYDLog.e(TAG, "handleResumeDir: mpeg4Name is " + mp4Name + " is err.");
                    continue;
                }

                BYDLog.d(TAG, "handleResumeDir: mpeg4Name is " + mp4Name);
                BYDLog.d(TAG, "handleResumeDir: mpeg4 cpy Name is " + cpyName);
                BYDLog.d(TAG, "handleResumeDir: getCurrentFile is " + getCurrentFile());
                if (getCurrentFile() != null
                        && (getCurrentFile().contains(cpyName)
                        || cpyName.contains(getCurrentFile()))) {
                    BYDLog.w(TAG, "handleResumeDir: the file[" + cpyName + "] is recording.");
                    continue;
                }
                if (VideoMpeg4File.check(mp4Name, MediaFormat.MIMETYPE_VIDEO_AVC)) {
                    BYDLog.w(TAG, "handleResumeDir: mpeg4File has  found mime " + MediaFormat.MIMETYPE_VIDEO_AVC);
                    BYDLog.w(TAG, "handleResumeDir: mpeg4File is good");
                    //Remove the copy file.
                    BYDLog.w(TAG, "handleResumeDir: will remove copy " + cpyName);
                    File cpyFile = new File(cpyName);
                    cpyFile.delete();
                    File indexFile = new File(indexName);
                    indexFile.delete();
                    notifyDbFileChanged(mp4Name);
                    continue;
                }
                //Remove the bad mp4 file.
                if (VideoMpeg4File.exists(mp4Name)) {
                    BYDLog.w(TAG, "handleResumeDir: mp4 file is bad, remove it");
                    VideoMpeg4File.remove(mp4Name);
                }
                File cpyFile = new File(cpyName);
                if (!cpyFile.exists()) {
                    BYDLog.w(TAG, "handleResumeDir: copy " + cpyName + " not exits");
                    BYDLog.w(TAG, "handleResumeDir: will remove indexName " + indexName);
                    File indexFile = new File(indexName);
                    indexFile.delete();
                    continue;
                }
                BYDLog.d(TAG, "handleResumeDir: copy " + cpyName + " length = " + cpyFile.length());
                //Cpy is good, rename to mp4
                if (VideoMpeg4File.check(cpyName, MediaFormat.MIMETYPE_VIDEO_AVC)) {
                    BYDLog.w(TAG, "handleResumeDir: mpeg4File has  found mime " + MediaFormat.MIMETYPE_VIDEO_AVC);
                    BYDLog.w(TAG, "handleResumeDir: mpeg4File is good");
                    //Remove the copy file.
                    BYDLog.w(TAG, "handleResumeDir: will remove copy " + cpyName);
                    File mp4File = new File(mp4Name);
                    cpyFile.renameTo(mp4File);
                    File indexFile = new File(indexName);
                    indexFile.delete();
                    continue;
                }
                //Copy data to new mp4 file(mpeg4Name) from copy file(copyName).
                try {
                    if (mDirMp4Reumser.resumeFrombadMp4(cpyName, indexName, mp4Name)) {
                        //Remove the copy.
                        BYDLog.e(TAG, "handleResumeDir: resume [" + mp4Name + "] sucessful from [" + cpyName + "]");
                        if (VideoMpeg4File.check(mp4Name, MediaFormat.MIMETYPE_VIDEO_AVC)) {
                            BYDLog.e(TAG, "handleResumMp4File: resume [" + mp4Name + "] sucessful from [" + cpyName + "]");
                            handleDelCopy(cpyName, mp4Name, DELETE_COPY_DELAY_TIME);
                            handleDelCopy(indexName, mp4Name, DELETE_COPY_DELAY_TIME);
                            resumeMp4PathList.add(mp4Name);
                            notifyDbFileChanged(mp4Name);
                            continue;
                        }
                    }
                    // resume fail and rm file and the index file.
                    BYDLog.e(TAG, "handleResumeDir: resume [" + mp4Name + "] fail from [" + cpyName + "]");
                    BYDLog.e(TAG, "handleResumeDir: delete copy file " + cpyName);
                    BYDLog.e(TAG, "handleResumeDir: delete index file " + indexName);
                    new File(cpyName).delete();
                    new File(indexName).delete();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return resumeMp4PathList;
    }

    /**
     * Covert copy name to mp4 name.
     * For example:
     * Copy name is "/sdcard/Recorder/Copy/xxx.mp4_index",
     * and result is "/sdcard/Recorder/Normal/xxx.mp4."
     *
     * @param indexName
     * @return
     */
    private String covertIndexToMpeg4File(String indexName) {
        BYDLog.d(TAG, "covertIndexToMpeg4File: E, indexName " + indexName);
        if (indexName == null || indexName.isEmpty() || !indexName.endsWith(SUFFIX_INDEX)) {
            BYDLog.e(TAG, "covertIndexToMpeg4File: the suffix index file is not .mp4_index!");
            return null;
        }
        String mpeg4Name = null;
        if (indexName.contains(FileSwapHelper.NORMAL)) {
            mpeg4Name = indexName.replace(FileSwapHelper.getVideoPath(FileSwapHelper.COPY), FileSwapHelper.getVideoPath(FileSwapHelper.NORMAL));
        } else if (indexName.contains(FileSwapHelper.LOCK)) {
            mpeg4Name = indexName.replace(FileSwapHelper.getVideoPath(FileSwapHelper.COPY), FileSwapHelper.getVideoPath(FileSwapHelper.LOCK));
        } else{
            mpeg4Name = indexName.replace(FileSwapHelper.getVideoPath(FileSwapHelper.COPY), FileSwapHelper.getVideoPath(FileSwapHelper.ROOT));
        }
        mpeg4Name = mpeg4Name.replace(SUFFIX_INDEX, SUFFIX_MP4);
        if (mpeg4Name == null || mpeg4Name.isEmpty() || !mpeg4Name.endsWith(SUFFIX_MP4)) {
            BYDLog.e(TAG, "covertIndexToMpeg4File: the suffix is not .mp4!");
            return null;
        }
        //mpeg4Name += PATH_NORMAL;//Need not add.
        BYDLog.d(TAG, "covertIndexToMpeg4File: X, mpeg4Name " + mpeg4Name);
        return mpeg4Name;
    }

    private String covertIndexToCpyFile(String indexName) {
        BYDLog.d(TAG, "covertIndexToCpyFile: E, indexName " + indexName);
        if (indexName == null || indexName.isEmpty() || !indexName.endsWith(SUFFIX_INDEX)) {
            BYDLog.e(TAG, "covertIndexToCpyFile: the suffix index file is not .mp4_index!");
            return null;
        }
        String mpeg4CpyName = null;
        if (indexName.contains(FileSwapHelper.NORMAL)) {
            mpeg4CpyName = indexName.replace(FileSwapHelper.getVideoPath(FileSwapHelper.COPY), FileSwapHelper.getVideoPath(FileSwapHelper.NORMAL));
        } else if (indexName.contains(FileSwapHelper.LOCK)) {
            mpeg4CpyName = indexName.replace(FileSwapHelper.getVideoPath(FileSwapHelper.COPY), FileSwapHelper.getVideoPath(FileSwapHelper.LOCK));
        } else{
            mpeg4CpyName = indexName.replace(FileSwapHelper.getVideoPath(FileSwapHelper.COPY), FileSwapHelper.getVideoPath(FileSwapHelper.ROOT));
        }

        mpeg4CpyName = mpeg4CpyName.replace(SUFFIX_INDEX, SUFFIX_MP4_CPY);
        if (mpeg4CpyName == null || mpeg4CpyName.isEmpty() || !mpeg4CpyName.endsWith(SUFFIX_MP4_CPY)) {
            BYDLog.e(TAG, "covertIndexToCpyFile: the suffix is not .mp4.cpy!");
            return null;
        }
        BYDLog.d(TAG, "covertIndexToCpyFile: X, mpeg4Name " + mpeg4CpyName);
        return mpeg4CpyName;
    }

    /**
     * Covert mp4 name to copy name.
     * For example:
     * Copy name is "/sdcard/Recorder/Copy/xxx.h264",
     * and result is "/sdcard/Recorder/Normal/xxx.mp4."
     *
     * @param fname
     * @return
     */
    private String covertMpeg4ToIndexFile(String fname) {
        BYDLog.d(TAG, "covertMpeg4ToIndexFile: E, fname " + fname);
        if (fname == null || fname.isEmpty() || !fname.endsWith(SUFFIX_MP4)) {
            BYDLog.e(TAG, "covertMpeg4ToIndexFile: the suffix is not .mp4!");
            return null;
        }
        String indexName = null;
//        if (fname.contains(FileSwapHelper.NORMAL)) {
//            indexName = fname.replace(FileSwapHelper.getVideoPath(FileSwapHelper.NORMAL), FileSwapHelper.getVideoPath(FileSwapHelper.COPY));
//        } else if (fname.contains(FileSwapHelper.LOCK)) {
//            indexName = fname.replace(FileSwapHelper.getVideoPath(FileSwapHelper.LOCK), FileSwapHelper.getVideoPath(FileSwapHelper.COPY));
//        } else {
//            indexName = fname.replace(FileSwapHelper.getVideoPath(FileSwapHelper.ROOT), FileSwapHelper.getVideoPath(FileSwapHelper.COPY));
//        }
        indexName = FileSwapHelper.getVideoIndexPath(fname);
//        indexName = indexName.replace(SUFFIX_MP4, SUFFIX_INDEX);
        BYDLog.v(TAG, "covertMpeg4ToIndexFile: copyName 2 " + indexName);
        if (indexName == null || indexName.isEmpty() || !indexName.endsWith(SUFFIX_INDEX)) {
            BYDLog.e(TAG, "covertMpeg4ToIndexFile: copyName is " + indexName + " is not valid");
            return null;
        }
        //mpeg4Name += PATH_NORMAL;//Need not add.
        BYDLog.v(TAG, "covertMpeg4ToIndexFile: X, copyName " + indexName);
        return indexName;
    }

    private String covertMpeg4ToCpyFile(String fname) {
        BYDLog.d(TAG, "covertMpeg4ToCpyFile: E, fname " + fname);
        if (fname == null || fname.isEmpty() || !fname.endsWith(SUFFIX_MP4)) {
            BYDLog.e(TAG, "covertMpeg4ToCpyFile: the suffix is not .mp4!");
            return null;
        }
        String copyName = fname.replace(SUFFIX_MP4, SUFFIX_MP4_CPY);
        BYDLog.d(TAG, "covertMpeg4ToCpyFile: copyName 2 " + copyName);
        if (copyName == null || copyName.isEmpty() || !copyName.endsWith(SUFFIX_MP4_CPY)) {
            BYDLog.e(TAG, "covertMpeg4ToCpyFile: copyName is " + copyName + " is not valid");
            return null;
        }
        //mpeg4Name += PATH_NORMAL;//Need not add.
        BYDLog.d(TAG, "covertMpeg4ToCpyFile: X, copyName " + copyName);
        return copyName;
    }
}
