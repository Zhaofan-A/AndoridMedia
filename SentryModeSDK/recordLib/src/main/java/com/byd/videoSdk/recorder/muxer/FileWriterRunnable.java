package com.byd.videoSdk.recorder.muxer;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.byd.videoSdk.common.handler.AutoVideoHandler;
import com.byd.videoSdk.common.handler.SentryModeHandlerMsg;
import com.byd.videoSdk.interfaces.IMp4DataWriter;
import com.byd.videoSdk.recorder.RecorderController;
import com.byd.videoSdk.recorder.buffer.DataBuffer;
import com.byd.videoSdk.recorder.encodec.VideoCodeEncoder;
import com.byd.videoSdk.recorder.utils.SDKMediaFormat;
import com.byd.videoSdk.recover.mdat.MDatWriter;
import com.byd.videoSdk.sdcard.SDCardStatus;
import com.byd.videoSdk.common.util.BYDLog;
import com.byd.videoSdk.recorder.utils.FileSwapHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class FileWriterRunnable implements Runnable {
    private String TAG = "FileWriterRunnable";
    public static final boolean DEBUG = true;

    private static final int DATA_THRESHOLD = 90;
    private static final int TIME_THRESHOLD = 5;
    public int mWaitTime = 0;
    private String mSavedFilename = null;
    private AutoVideoHandler mAutoVideoHandler = null;
    private MediaFormat videoMediaFormat = null;
    private DataBuffer muxerDatas;
    private volatile boolean isExit = false;
    private boolean mFoundFirstKeyFrame = false;
    private long mFirstPresentationTimeUs = 0;
    private long mRecordingTime = 1 * 60 * 1000 * 1000; // total record time 1分钟
    private long mAlreadyRecordTime = 0; // already record time
    private boolean mbStartOK = true;
    private boolean mLoopRecord = false;  // is loop record

    private IMp4DataWriter mp4DataWriter = null;
    private String loopType;


    public FileWriterRunnable(String savaName) {
        this("", savaName, false);
    }

    public FileWriterRunnable(String prefix, boolean isLoopRecord) {
        this(prefix, null, isLoopRecord);
    }

    public FileWriterRunnable(String prefix, String savaName, boolean isLoopRecord) {
        TAG = prefix + TAG;
        loopType = prefix;
        mLoopRecord = isLoopRecord;
        mSavedFilename = savaName;
        muxerDatas = new DataBuffer(3, 3);
        muxerDatas.clear();

        mFoundFirstKeyFrame = false;
        mAutoVideoHandler = AutoVideoHandler.getInstance();
        BYDLog.d(TAG, "FileWriterRunnable created  " + mSavedFilename);
    }

    public boolean isStart() {
        BYDLog.d(TAG, "FileWriterRunnable mbStartOK = " + mbStartOK);
        return mbStartOK;
    }

    private void init() {
        BYDLog.d(TAG, "initMuxer");
        restart();
    }

    private void restart() {
        try {
            isExit = false;
            reset();
            synchronized (this) {
                notifyAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
            BYDLog.e(TAG, "restartMediaMuxer: error");
        }
    }

    private void stop() {
        BYDLog.d(TAG, "stop: E");
        mbStartOK = false;
        if (mp4DataWriter != null) {
            mp4DataWriter.close();
            notifyStopFileChanged();
        }
        mp4DataWriter = null;
        BYDLog.d(TAG, "stop: X");
    }

    private void reset() throws Exception {
        stop();
        if (mLoopRecord) {
            mSavedFilename = FileSwapHelper.getNextFileName(loopType);
            mRecordingTime = RecorderController.getInstance().getloopRecordingTime();
        }
        mRecordingTime = RecorderController.getInstance().getRecordingTime();
        BYDLog.d(TAG, "reset: mRecordingTime = " + mRecordingTime);
        BYDLog.d(TAG, "reset: mLoopRecord = " + mLoopRecord + "    mSavedFilename = " + mSavedFilename);
        mFoundFirstKeyFrame = false;
        mp4DataWriter = RecorderController.getInstance().createMpeDataWriter(mSavedFilename);
        notifyStartFileChanged();

        if (mp4DataWriter != null && videoMediaFormat != null ) {
            //循环录制时第二次及后续都会走这里
            BYDLog.d(TAG, "reset: videoMediaFormat is " + videoMediaFormat);
            mp4DataWriter.setVideoMediaFormat(videoMediaFormat);
        }
        mbStartOK = true;
    }

    /**
     * Write data to copy file.
     *
     * @param byteBuf
     * @param bufferInfo
     * @return
     */
    private boolean writeCopyFile(ByteBuffer byteBuf, BufferInfo bufferInfo) {
        BYDLog.d(TAG, "writeCopyFile: byteBuf " + byteBuf + "\n  bufferInfo " + bufferInfo);
        if (byteBuf == null || bufferInfo == null) {
            BYDLog.e(TAG, "writeCopyFile: input is null;");
            return false;
        }
        BYDLog.d(TAG, "writeCopyFile: byteBuf arrayOffset() " + byteBuf.arrayOffset() + " \nbyteBuf capacity() " + byteBuf.capacity());
        BYDLog.d(TAG, "writeCopyFile: mp4DataWriter is null " + (mp4DataWriter == null));
        if (mp4DataWriter != null) {
            mp4DataWriter.writeSampeData(byteBuf, bufferInfo);
            return true;
        }
        BYDLog.e(TAG, "run: mp4DataWriter is null");
        return false;
    }

    public synchronized void setMediaFormat(MediaFormat mediaFormat) {
        if (mp4DataWriter == null) {
            BYDLog.e(TAG, "mediaMuxer is null, return!");
            return;
        }
        if (mediaFormat != null) {
            videoMediaFormat = mediaFormat;
            BYDLog.d(TAG, "setMediaFormat() videoMediaFormat = " + videoMediaFormat);
            if (mp4DataWriter != null) {
                mp4DataWriter.setVideoMediaFormat(mediaFormat);
            }
        }
        notifyAll();
    }

    public synchronized void addData(VideoCodeEncoder.MediaCodecData data) {
        BYDLog.v(TAG, "addData");
        if (isExit || data == null || muxerDatas == null) {
            BYDLog.e(TAG, "addData: data is " + data);
            BYDLog.e(TAG, "addData: muxerDatas is " + muxerDatas);
            return;
        }
        boolean isKeyFrame = (data.bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME);
        BYDLog.d(TAG, "addData: isKeyFrame " + isKeyFrame + "     muxerDatas.size() = " + muxerDatas.size());

        if (videoMediaFormat == null) {
            //开启第二个录制时因为format已经回调过一次，所以第二个录像需要主动获取format
            BYDLog.d(TAG, "addData: videoMediaFormat is null " );
            videoMediaFormat = RecorderController.getInstance().getVideoMediaFormat();
            if (mp4DataWriter != null ) {
                if(videoMediaFormat == null){
                    videoMediaFormat = SDKMediaFormat.getMPEG4MediaFormat();
                }
                BYDLog.d(TAG, "addData: videoMediaFormat is " + videoMediaFormat);
                mp4DataWriter.setVideoMediaFormat(videoMediaFormat);
            }
        }
        muxerDatas.addMediaCodecData(data);
        try {
            if (muxerDatas.size() > 2) {
                if (muxerDatas.size() > DATA_THRESHOLD && !mbStartOK) {
                    notifyAll();
                }
            }
        } catch (Exception e) {
            BYDLog.w(TAG, "addData", e);
        }

        BYDLog.d(TAG, "addData: --------");
        if (mbStartOK) {
            notifyAll();
        }
    }

    public synchronized void exit() {
        isExit = true;
        mbStartOK = false;
        BYDLog.d(TAG, "isExit = " + isExit);
        if (muxerDatas != null) {
            muxerDatas.clear();
        }
        mWaitTime = 0;
        notifyAll();
    }

    /**
     * get lock left time
     *
     * @return
     */
    public synchronized long getAlreadyRecordTime() {
        BYDLog.d(TAG, "getLockLeftTime mRecordingTime = " + mRecordingTime +
                " mAlreadyRecordTime = " + mAlreadyRecordTime);
        return mAlreadyRecordTime;
    }

    @Override
    public void run() {
        mWaitTime = 0;
        init();
        BYDLog.i(TAG, "run() isExit = " + isExit);
        while (!isExit && SDCardStatus.getInstance().isTFlashCardExists()) {
            BYDLog.d(TAG, "run() mbStartOK = " + mbStartOK);
            if (mbStartOK) {
                synchronized (this) {
                    if (muxerDatas.isEmpty()) {
                        try {
                            if (DEBUG) BYDLog.v(TAG, "run:muxerDatas is empty! wait...");
                            wait(2000);
                        } catch (InterruptedException e) {
                            BYDLog.e(TAG, "EXCEPTION:" + e.toString());
                        }
                    }
                }

                if (isExit) {
                    BYDLog.d(TAG, "thread is asked to exit!");
                    break;
                }
                if (muxerDatas.isEmpty()) {
                    BYDLog.e(TAG, "run: has not data yet!");
                    continue;
                }
                VideoCodeEncoder.MediaCodecData data = muxerDatas.remove(0);
                if (data == null) {
                    continue;
                }
                if (!mFoundFirstKeyFrame) {
                    BYDLog.v(TAG, "run: frame flag is " + data.bufferInfo.flags + " , size " + data.bufferInfo.size);
                    if (data.bufferInfo.flags != MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                        BYDLog.v(TAG, "run: not found the first key frame yet, discard this frame!");
                        continue;
                    }
                    mFoundFirstKeyFrame = true;
                    mFirstPresentationTimeUs = data.bufferInfo.presentationTimeUs;
                    BYDLog.d(TAG, "run: Found First Key Frame, mFirstPresentationTimeUs = " + mFirstPresentationTimeUs + " , mRecordingTime = " + mRecordingTime);
                }

                try {
                    long writeCopyFileStartTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
                    BYDLog.d(TAG, "run: data.bufferInfo.size = " + data.bufferInfo.size);
                    boolean ret = writeCopyFile(data.byteBuf, data.bufferInfo);
                    long writeCopyFileCostTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - writeCopyFileStartTime;
                    if (writeCopyFileCostTime > 3 * 1000) {
                        BYDLog.d(TAG, "run:writeCopyFile cost long time " + writeCopyFileCostTime + " MS");
                    } else {
                        BYDLog.v(TAG, "run:writeCopyFile cost time " + writeCopyFileCostTime + " MS");
                    }
                } catch (Exception e) {
                    BYDLog.e(TAG, "run:EXCEPTION:" + e.toString());
                    if (!isExit) {
                        restart();
                        continue;
                    } else {
                        BYDLog.e(TAG, "run: isExits!");
                        break;
                    }
                }
                if (DEBUG) BYDLog.v(TAG, "run:buffer size:" + data.bufferInfo.size);
                mAlreadyRecordTime = data.bufferInfo.presentationTimeUs - mFirstPresentationTimeUs;
                BYDLog.d(TAG, "run: mAlreadyRecordTime " + mAlreadyRecordTime + "   " + mRecordingTime);
                if (mAlreadyRecordTime >= mRecordingTime) {
                    BYDLog.d(TAG, "run: presentationTimeUs end = " + data.bufferInfo.presentationTimeUs + " , mFirstPresentationTimeUs = " + mFirstPresentationTimeUs + ", mRecordingTime = " + mRecordingTime +
                            " , during = " + (data.bufferInfo.presentationTimeUs - mFirstPresentationTimeUs));
                    if (!mLoopRecord) {
                        break;
                    }
                    restart();
                    BYDLog.d(TAG, "run: The data is key frame, so reset muxer new file!");
                    continue;
                }
            } else {
                BYDLog.d(TAG, "run() mbStartOK = " + mbStartOK);
                try {
                    if ((muxerDatas.size() >= DATA_THRESHOLD)
                            || (mWaitTime >= TIME_THRESHOLD && mWaitTime % TIME_THRESHOLD == 0)) {
                        BYDLog.w(TAG, " will notofy message MSG_ERR_NOT_MEDIA_FORMAT");
                        synchronized (this) {
                            wait(1000);
                        }
                    } else {
                        BYDLog.i(TAG, "media muxer isn't start, wait...");
                        synchronized (this) {
                            wait(2000);
                        }
                    }
                    mWaitTime++;
                } catch (InterruptedException e) {
                    BYDLog.e(TAG, "EXCEPTION:" + e.toString());
                }
            }
        }
        BYDLog.d(TAG, "stopMediaMuxer");
        stop();
        BYDLog.w(TAG, "MUXER exit");
    }

    /**
     * getSavedFilename
     *
     * @return
     */
    public String getSavedFilename() {
        return mSavedFilename;
    }

    /**
     * For Current start recorder file.
     */
    private void notifyStartFileChanged() {
        if (!SDCardStatus.getInstance().isTFlashCardExists()) {
            BYDLog.e(TAG, "notifyDbFileChanged() SD card eject, return!");
            return;
        }
        BYDLog.d(TAG, "notifyStartFileChanged mSavedFilename = " + mSavedFilename);
        int what = SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD_VIDEO_START;
        BYDLog.d(TAG, "msg is = " + SentryModeHandlerMsg.getMessageName(what));
        if (mSavedFilename != null && mAutoVideoHandler != null) {
            mAutoVideoHandler.sendDirectlyMessageAsync(what, mSavedFilename);
        }
    }

    /**
     * Stop recorder files.
     */
    private void notifyStopFileChanged() {
        if (!SDCardStatus.getInstance().isTFlashCardExists()) {
            BYDLog.d(TAG, "notifyStopFileChanged() SD card eject, return!");
            return;
        }
        BYDLog.d(TAG, "notifyStopFileChanged mSavedFilename = " + mSavedFilename);
        int what = SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD_VIDEO_END;
        BYDLog.d(TAG, "msg is = " + SentryModeHandlerMsg.getMessageName(what));
        if (mSavedFilename != null && mAutoVideoHandler != null) {
            BYDLog.d(TAG, "notifyStopFileChanged: start recording " + mSavedFilename);
            mAutoVideoHandler.sendDirectlyMessageAsync(what, mSavedFilename);
        }
    }
}
