package com.byd.videoSdk.recorder.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.byd.videoSdk.common.handler.AutoVideoHandler;
import com.byd.videoSdk.common.handler.SentryModeHandlerMsg;
import com.byd.videoSdk.interfaces.IMp4DataWriter;
import com.byd.videoSdk.recorder.file.VideoMpeg4File;
import com.byd.videoSdk.recover.VideoBackupHandler;
import com.byd.videoSdk.sdcard.SDCardStatus;
import com.byd.videoSdk.common.util.BYDLog;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class Mp4DataWriter implements IMp4DataWriter {
    private static final String TAG = "Mp4DataWriter";

    private VideoMpeg4File mMp4Writer;
    private boolean mIsInit = false;
    private String mp4Name;

    public Mp4DataWriter(String name) throws IOException {
        mp4Name =  name;

        if (null == assertPath(mp4Name)) {
            throw new IOException("The cpyName path " + mp4Name + " is err.");
        }

        mMp4Writer = new VideoMpeg4File(name);
    }

    @Override
    public boolean setVideoMediaFormat(MediaFormat f) {
        if (mIsInit) {
            BYDLog.e(TAG, "setVideoMediaFormat: X, err has inited!");
            return false;
        }
        if (mMp4Writer != null) {
            mIsInit = true;
            return mMp4Writer.setVideoMediaFormat(f);
        }
        return false;
    }

    @Override
    public void writeSampeData(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        if (!mIsInit) {
            BYDLog.e(TAG, "writeSampeData: X, err not init!");
        }
        writeToFile(buffer, info);
    }

    private void  writeToFile(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        BYDLog.d(TAG, "writeToBuffer: E, mMp4Writer " + mMp4Writer );
        if (mMp4Writer != null) {
            mMp4Writer.writeSampeData(buffer, info);

            syncFile();
        }else{
            BYDLog.w(TAG, "writeToBuffer: X, write fail!");
        }
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
            BYDLog.v(TAG, "sync: X, end sync data.");
            return true;
        }
        BYDLog.e(TAG, "sync: X, ERR.");
        return false;
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
        }
        BYDLog.d(TAG, "closePrivate:X");
        BYDLog.d(TAG, "close: 视频落盘成功 !");

        int what = SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD_VIDEO_SYNC_END;
        AutoVideoHandler.getInstance().sendDirectlyMessageAsync(what, -1, -1, mp4Name);
    }

    @Override
    public void close() {
        BYDLog.d(TAG, "close: ");
        if (!VideoBackupHandler.getInstance().hasRunnable(mCloseRunnable)) {
            VideoBackupHandler.getInstance().postRunnable(mCloseRunnable);
        }
    }

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
