package com.byd.videoSdk.recover.mdat;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.byd.videoSdk.common.handler.AutoVideoHandler;
import com.byd.videoSdk.common.handler.SentryModeHandlerMsg;
import com.byd.videoSdk.interfaces.IMp4DataWriter;
import com.byd.videoSdk.recorder.RecorderController;
import com.byd.videoSdk.recorder.utils.SDKMediaFormat;
import com.byd.videoSdk.recover.VideoBackupHandler;
import com.byd.videoSdk.recorder.file.VideoMpeg4File;
import com.byd.videoSdk.recorder.utils.SharedPreferencesManager;
import com.byd.videoSdk.sdcard.SDCardStatus;
import com.byd.videoSdk.common.util.BYDLog;
import com.byd.videoSdk.recorder.utils.FileSwapHelper;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * MDatWriter.
 *
 * @author bai.yu1
 */
public class MDatWriter implements IMp4DataWriter {
    private static final String TAG = "MDatWriter";

    private MDatFrameIndexWriter mDataFrameIndexWriter;
    private VideoMpeg4File mMp4Writer;
    private boolean mIsInit = false;
    String indexName;
    String mp4Name;


    public MDatWriter(String name) throws IOException {
        String cpyName = null;
        indexName = null;
        mp4Name = name;
        BYDLog.d(TAG, "MDatWriter: name = " + name);
        if (name != null && name.contains(FileSwapHelper.BASE_EXT)) {
            cpyName = name.replace(FileSwapHelper.BASE_EXT, FileSwapHelper.MP4_CPY_EXT);
//            fileName = name.replace(FileSwapHelper.BASE_EXT, FileSwapHelper.MP4_INDEX_EXT);
//            if (name.contains(FileSwapHelper.NORMAL)) {
//                indexName = indexName.replace(FileSwapHelper.getVideoPath(FileSwapHelper.NORMAL), FileSwapHelper.getVideoPath(FileSwapHelper.COPY));
//            } else if (name.contains(FileSwapHelper.LOCK)) {
//                indexName = indexName.replace(FileSwapHelper.getVideoPath(FileSwapHelper.LOCK), FileSwapHelper.getVideoPath(FileSwapHelper.COPY));
//            } else {
//                indexName = indexName.replace(FileSwapHelper.getVideoPath(FileSwapHelper.ROOT), FileSwapHelper.getVideoPath(FileSwapHelper.COPY));
//            }
            indexName = FileSwapHelper.getVideoIndexPath(mp4Name);
        } else {
            cpyName = name + FileSwapHelper.MP4_CPY_EXT;
//            indexName = name + FileSwapHelper.MP4_INDEX_EXT;
//            if (name.contains(FileSwapHelper.NORMAL)) {
//                indexName = indexName.replace(FileSwapHelper.getVideoPath(FileSwapHelper.NORMAL), FileSwapHelper.getVideoPath(FileSwapHelper.COPY));
//            } else if (name.contains(FileSwapHelper.LOCK)) {
//                indexName = indexName.replace(FileSwapHelper.getVideoPath(FileSwapHelper.LOCK), FileSwapHelper.getVideoPath(FileSwapHelper.COPY));
//            } else {
//                indexName = indexName.replace(FileSwapHelper.getVideoPath(FileSwapHelper.ROOT), FileSwapHelper.getVideoPath(FileSwapHelper.COPY));
//            }
            indexName = FileSwapHelper.getVideoIndexPath(mp4Name);
        }
        if (null == assertPath(cpyName)) {
            throw new IOException("The cpyName path " + cpyName + " is err.");
        }
        if (null == assertPath(indexName)) {
            throw new IOException("The index path " + indexName + " is err.");
        }
        BYDLog.d(TAG, "MDatWriter:   cpyName= " + cpyName + "   indexName=" + indexName);
        mMp4Writer = new VideoMpeg4File(cpyName);
        mDataFrameIndexWriter = new MDatFrameIndexWriter(indexName);
    }

    @Override
    public synchronized boolean setVideoMediaFormat(MediaFormat f) {
        if (mIsInit) {
            BYDLog.e(TAG, "setVideoMediaFormat: X, err has inited!");
            return false;
        }
        if (mMp4Writer != null) {
            mIsInit = true;
            if (mMp4Writer.setVideoMediaFormat(f)) {
                if (mDataFrameIndexWriter != null) {
                    BYDLog.d(TAG, "MdatWriter_setVideoMediaFormat: ");
                    ByteBuffer spsb = f.getByteBuffer("csd-0");
                    ByteBuffer ppsb = f.getByteBuffer("csd-1");
                    BYDLog.d(TAG, "setVideoMediaFormat: ");

                    BYDLog.d(TAG, "setVideoMediaFormatToSp: spsb = " + Arrays.toString(spsb.array()));
                    BYDLog.d(TAG, "setVideoMediaFormatToSp: ppsb = " + Arrays.toString(ppsb.array()));
                    if (spsb != null && ppsb != null) {
                        mDataFrameIndexWriter.writeInt(spsb.array().length);
                        mDataFrameIndexWriter.writeToFile(spsb.array());
                        mDataFrameIndexWriter.writeInt(ppsb.array().length);
                        mDataFrameIndexWriter.writeToFile(ppsb.array());

                        SharedPreferencesManager.getInstance(RecorderController.getInstance().getContext()).putString(SDKMediaFormat.KEY_SPS, Arrays.toString(spsb.array()));
                        SharedPreferencesManager.getInstance(RecorderController.getInstance().getContext()).putString(SDKMediaFormat.KEY_PPS, Arrays.toString(ppsb.array()));
                    }
                }
                return true;
            }
        }
        return false;
    }


    private void writeToFile(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        BYDLog.d(TAG, "writeToBuffer: E, mMp4Writer " + mMp4Writer + "!" + "   " + indexName);
        if (mMp4Writer != null) {
            if (mMp4Writer.writeSampeData(buffer, info)) {
                BYDLog.v(TAG, "writeToBuffer: mDataFrameIndexWriter " + mDataFrameIndexWriter + "!");
                if (mDataFrameIndexWriter != null) {
                    BYDLog.d(TAG, "writeToBuffer: X, write buffer and index OK!");
                    mDataFrameIndexWriter.writeBuffer(info);
                } else {
                    BYDLog.w(TAG, "writeToBuffer: X, write buffer OK but not index file!");
                }
            }
            syncFile();
        } else {
            BYDLog.w(TAG, "writeToBuffer: X, write fail!");
        }
    }

    @Override
    public synchronized void writeSampeData(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        if (!mIsInit) {
            BYDLog.e(TAG, "writeSampeData: X, err not init!");
        }
        writeToFile(buffer, info);
    }


    private boolean syncFile() {
        BYDLog.v(TAG, "sync: will post runnbale!");
        if (!VideoBackupHandler.getInstance().hasRunnable(mSyncRunnable)
                && !VideoBackupHandler.getInstance().hasRunnable(mCloseRunnable)) {
            VideoBackupHandler.getInstance().postRunnable(mSyncRunnable);
        }
        BYDLog.v(TAG, "sync: end post runnbale!");
        return true;
    }

    private boolean syncPrivate() {
        BYDLog.v(TAG, "sync: E, init " + mIsInit + "!");
        if (!mIsInit) {
            BYDLog.e(TAG, "sync: X, err not init!");
            return false;
        }
        if (mMp4Writer != null) {
            BYDLog.v(TAG, "sync: will sync mdat data.");
            long mp4WriteSyncStartTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            mMp4Writer.sync();
            long mp4WriteSyncCostTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - mp4WriteSyncStartTime;
            if (mp4WriteSyncCostTime >= SYNC_COST_TIME) {
                BYDLog.d(TAG, "syncPrivate: mp4Writer sync cost long time " + mp4WriteSyncCostTime + " MS");
            } else {
                BYDLog.v(TAG, "syncPrivate: mp4Writer sync cost time " + mp4WriteSyncCostTime + " MS");
            }
            BYDLog.v(TAG, "sync: will sync index data.");
            long frameIndeSyncStartTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            if (mDataFrameIndexWriter != null) {
                mDataFrameIndexWriter.sync();
            }
            long frameIndeSyncCostTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - frameIndeSyncStartTime;
            if (frameIndeSyncCostTime >= SYNC_COST_TIME) {
                BYDLog.d(TAG, "syncPrivate: frameIndex sync cost long time " + frameIndeSyncCostTime + " MS");
            } else {
                BYDLog.v(TAG, "syncPrivate: frameIndex sync cost time " + frameIndeSyncCostTime + " MS");
            }
            BYDLog.v(TAG, "sync: X, end sync data.");
            return true;
        }
        BYDLog.e(TAG, "sync: X, ERR.");
        return false;
    }

    @Override
    public void close() {
        Log.d(TAG, "close: ");
        if (!VideoBackupHandler.getInstance().hasRunnable(mCloseRunnable)) {
            VideoBackupHandler.getInstance().postRunnable(mCloseRunnable);
        }
    }

    private void closePrivate() {
        BYDLog.d(TAG, "closePrivate:E");
        synchronized (this) {
            mIsInit = false;
            BYDLog.d(TAG, "closePrivate: mMp4Writer close start.");
            if (mMp4Writer != null) {
                mMp4Writer.close();
                mMp4Writer = null;
            }
            BYDLog.d(TAG, "closePrivate: mDataFrameIndexWriter close start.");
            if (mDataFrameIndexWriter != null) {
                mDataFrameIndexWriter.closeFile();
                mDataFrameIndexWriter = null;
            }
        }
        BYDLog.d(TAG, "closePrivate:X");

        BYDLog.d(TAG, "close: 视频落盘成功 !");

        int what = SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD_VIDEO_SYNC_END;
        AutoVideoHandler.getInstance().sendDirectlyMessageAsync(what, -1, -1, mp4Name);
    }

    private Runnable mSyncRunnable = new Runnable() {
        public void run() {
            syncPrivate();
        }
    };
    private Runnable mCloseRunnable = new Runnable() {
        public void run() {
            if (SDCardStatus.getInstance().isTFlashCardExists()) {
                BYDLog.d(TAG, "mCloseRunnable start");
                syncPrivate();
                BYDLog.d(TAG, "mCloseRunnable end");
            }
            closePrivate();
        }
    };

    private String assertPath(String fileName) {
        StringBuilder fullPath = new StringBuilder();
        fullPath.append(fileName);

        String string = fullPath.toString();
        BYDLog.d(TAG, "assertPath:fullPath = " + string);
        File file = new File(string);
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                BYDLog.d(TAG, "mkdirs fail");
                return null;
            }
        }
        return string;
    }
}
