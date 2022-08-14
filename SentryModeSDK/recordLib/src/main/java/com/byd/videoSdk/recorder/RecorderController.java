package com.byd.videoSdk.recorder;

import android.content.Context;
import android.content.Intent;
import android.media.MediaFormat;

import com.byd.videoSdk.camera.CameraEglSurfaceView;
import com.byd.videoSdk.common.handler.AutoVideoHandler;
import com.byd.videoSdk.common.handler.AutoVideoHandlerListener;
import com.byd.videoSdk.common.handler.SentryModeHandler;
import com.byd.videoSdk.common.handler.SentryModeHandlerMsg;
import com.byd.videoSdk.common.util.BYDLog;
import com.byd.videoSdk.egl.EglController;
import com.byd.videoSdk.interfaces.IMp4DataWriter;
import com.byd.videoSdk.recorder.encodec.VideoCodeEncoder;
import com.byd.videoSdk.recorder.listener.OnAddWatermarkListener;
import com.byd.videoSdk.recorder.listener.VideoStateListener;
import com.byd.videoSdk.recorder.muxer.FileWriterRunnable;
import com.byd.videoSdk.recorder.muxer.Mp4DataWriter;
import com.byd.videoSdk.recorder.utils.Constants;
import com.byd.videoSdk.recorder.utils.FileSwapHelper;
import com.byd.videoSdk.recorder.utils.RecordConfig;
import com.byd.videoSdk.recover.Mp4ResumeController;
import com.byd.videoSdk.recover.mdat.MDatWriter;
import com.byd.videoSdk.service.SdkService;
import com.byd.videoSdk.watermark.WatermarkBitmapInfo;
import com.byd.videoSdk.watermark.WatermarkFactor;
import com.byd.videoSdk.watermark.WatermarkRender;
import com.byd.videoSdk.watermark.WatermarkTextInfo;
import com.byd.videoSdk.watermark.WatermarkTimeInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLContext;

public class RecorderController implements VideoCodeEncoder.OnVideoCodeCallback, AutoVideoHandlerListener {

    private static final String TAG = "RecorderController";

    private boolean mRecorverEnable = true;
    private long mRecordingTime = 60 * 1000 * 1000; // total record time 1分钟
    private long mLoopRecordingTime = 60 * 1000 * 1000; // total record time 1分钟

    private List<VideoStateListener> videoStateListeners = new ArrayList<>();
    private Map<String, FileWriterRunnable> fileWriterRunnableMap = new HashMap<>();

    private Context mContext;
    private Intent mIntent;

    private static RecorderController recorderController;

    private VideoCodeEncoder videoEncodeRecode;
    private WatermarkRender mWatermarkRender;
    private MediaFormat videoMediaFormat = null;

    private RecorderController() {
    }

    public static RecorderController getInstance() {
        if (recorderController == null) {
            recorderController = new RecorderController();
        }
        return recorderController;
    }

    public void init(Context context) {
        BYDLog.d(TAG, "init: ");
        this.mContext = context.getApplicationContext();
        Mp4ResumeController.getInstance();
        AutoVideoHandler.getInstance().registerHandlerListener(this, SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD);

        mIntent = new Intent(context, SdkService.class);
        context.startService(mIntent);
    }

    public void release() {
        BYDLog.d(TAG, "release: ");
        if (mContext == null) {
            BYDLog.e(TAG, "release: Please initialize first ! ");
            try {
                throw new Exception("The SDK has not been initialized yet！");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stopRecord();
        mContext.stopService(mIntent);
        recorderController = null;
    }

    public void setConfig(RecordConfig recordConfig) {
        BYDLog.d(TAG, "setConfig: recordConfig = " + recordConfig.toString());
        Constants.setRecordConfig(recordConfig);
    }

    /**
     * 预览循环录制,默认一分钟录一个视频
     *
     * @param type 录制类型
     */
    public void starLoopRecord(String type, CameraEglSurfaceView cameraEglSurfaceView) {
        BYDLog.d(TAG, "starLoopRecord: type = " + type);
        if (cameraEglSurfaceView != null) {
            if (!isLoopStartRecord()) {
                FileWriterRunnable fileWriterRunnable = new FileWriterRunnable(type, true);
                fileWriterRunnableMap.put(FileSwapHelper.NORMAL, fileWriterRunnable);
                new Thread(fileWriterRunnable).start();
                startEncode(cameraEglSurfaceView.getTextureId(), cameraEglSurfaceView.getEglContext());
            }
        } else {
            BYDLog.e(TAG, "CameraEglSurfaceView is null ");
        }
    }

    /**
     * 预览录制，默认录制一分钟就会停止录制
     *
     * @param videoName
     * @param cameraEglSurfaceView
     */
    public void starRecord(String videoName, CameraEglSurfaceView cameraEglSurfaceView) {
        BYDLog.d(TAG, "starRecord: videoName = " + videoName);
        if (cameraEglSurfaceView != null) {
            if (!isStartRecord()) {
                String videoAbsPath = FileSwapHelper.getVideoPath(FileSwapHelper.ROOT) + videoName;
                FileWriterRunnable fileWriterRunnable = new FileWriterRunnable(videoAbsPath);
                fileWriterRunnableMap.put(FileSwapHelper.LOCK, fileWriterRunnable);
                new Thread(fileWriterRunnable).start();
                startEncode(cameraEglSurfaceView.getTextureId(), cameraEglSurfaceView.getEglContext());
            }
        } else {
            BYDLog.e(TAG, "CameraEglSurfaceView is null ");
        }
    }

    /**
     * 无预览录制，默认录制一分钟就会停止录制
     *
     * @param videoName
     */
    public void starNoPreviewRecord(final String videoName, final OnAddWatermarkListener onAddWatermarkListener) {
        BYDLog.d(TAG, "starNoPreviewRecord: videoName = " + videoName);
        if (!isStartRecord()) {
            EglController.getInstance().startCamera(mContext);
            EglController.getInstance().setEglInitListener(new EglController.EglInitListener() {
                @Override
                public void onEglCreated() {
                    String videoAbsPath = FileSwapHelper.getVideoPath(FileSwapHelper.ROOT) + videoName;
                    FileWriterRunnable fileWriterRunnable = new FileWriterRunnable(videoAbsPath);
                    fileWriterRunnableMap.put(FileSwapHelper.LOCK, fileWriterRunnable);
                    new Thread(fileWriterRunnable).start();
                    startEncode(EglController.getInstance().getTextureId(), EglController.getInstance().getEglContext());
                    if (onAddWatermarkListener != null) {
                        onAddWatermarkListener.onAddWatermark();
                    }
                }
            });
        }
    }

    /**
     * 无预览循环录制，默认录制一分钟就会停止录制
     */
    public void starNoPreviewLoopRecord(final String type, final OnAddWatermarkListener onAddWatermarkListener) {
        BYDLog.d(TAG, "starNoPreviewLoopRecord:  ");
        if (!isLoopStartRecord()) {
            EglController.getInstance().startCamera(mContext);
            EglController.getInstance().setEglInitListener(new EglController.EglInitListener() {
                @Override
                public void onEglCreated() {
                    FileWriterRunnable fileWriterRunnable = new FileWriterRunnable(type, true);
                    fileWriterRunnableMap.put(FileSwapHelper.NORMAL, fileWriterRunnable);
                    new Thread(fileWriterRunnable).start();
                    startEncode(EglController.getInstance().getTextureId(), EglController.getInstance().getEglContext());
                    if (onAddWatermarkListener != null) {
                        onAddWatermarkListener.onAddWatermark();
                    }
                }
            });
        }
    }

    private void startEncode(int textureId, EGLContext eglContext) {
        if (mContext == null) {
            try {
                throw new Exception("The SDK has not been initialized yet！");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BYDLog.d(TAG, "startEncode: " + Constants.IMAGE_WIDTH + "   " + Constants.IMAGE_HEIGHT);
        if (videoEncodeRecode != null && videoEncodeRecode.isEncodeStart()) {
            BYDLog.d(TAG, "startEncode: encode already start");
            return;
        }
        videoEncodeRecode = new VideoCodeEncoder(mContext);
        videoEncodeRecode.initEncoder(eglContext, textureId, Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGHT);
        videoEncodeRecode.registerCallback(this);
        videoEncodeRecode.startRecode();

        //录像水印
        mWatermarkRender = WatermarkFactor.getInstance(mContext).getWatermarkRender(WatermarkFactor.RECORD_WATERMARK_RENDER);
        videoEncodeRecode.setWatermarkRender(mWatermarkRender);
    }

    public void stopRecord() {
        BYDLog.d(TAG, "stopRecord: ");
        EglController.getInstance().release();
        if (fileWriterRunnableMap.size() == 1) {
            stopEncode();
        }
        FileWriterRunnable fileWriterRunnable = fileWriterRunnableMap.get(FileSwapHelper.LOCK);
        if (fileWriterRunnable != null) {
            fileWriterRunnable.exit();
            fileWriterRunnableMap.remove(FileSwapHelper.LOCK);
        }
    }

    public void stopLoopRecord() {
        BYDLog.d(TAG, "stopLoopRecord: ");
        EglController.getInstance().release();
        if (fileWriterRunnableMap.size() == 1) {
            stopEncode();
        }
        FileWriterRunnable fileWriterRunnable = fileWriterRunnableMap.get(FileSwapHelper.NORMAL);
        if (fileWriterRunnable != null) {
            fileWriterRunnable.exit();
            fileWriterRunnableMap.remove(FileSwapHelper.NORMAL);
        }
    }

    private void stopEncode() {
        BYDLog.d(TAG, "stopEncode: ");
        if (videoEncodeRecode != null) {
            videoEncodeRecode.unregisterCallback(this);
            videoEncodeRecode.stopRecode();
            videoEncodeRecode = null;
        }
    }

    public boolean isStartRecord() {
        FileWriterRunnable fileWriterRunnable = getFileWriterRunnable(FileSwapHelper.LOCK);
        boolean istart = fileWriterRunnable != null;
        BYDLog.e(TAG, "isStartRecord: istart = " + istart);
        return istart;
    }

    public boolean isLoopStartRecord() {
        FileWriterRunnable fileWriterRunnable = getFileWriterRunnable(FileSwapHelper.NORMAL);
        boolean istart = fileWriterRunnable != null;
        BYDLog.e(TAG, "isLoopStartRecord: istart = " + istart);
        return istart;
    }

    public IMp4DataWriter createMpeDataWriter(String savedFilename) {
        IMp4DataWriter mp4DataWriter = null;
        try {
            if (mRecorverEnable) {
                mp4DataWriter = new MDatWriter(savedFilename);
            } else {
                mp4DataWriter = new Mp4DataWriter(savedFilename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mp4DataWriter;
    }


    public void setSavePath(String savaPath) {
        if (savaPath == null) {
            BYDLog.e(TAG, "setSavePath: savaPath is null ! ");
        }
        FileSwapHelper.setRootPath(savaPath);
    }

    public String getDefaultSavePath(){
        return FileSwapHelper.getVideoPath(null);
    }


    /**
     * 设置录制循环视频单个时长,单位：秒
     */
    public synchronized void setLoopRecordTime(int time) {
        BYDLog.d(TAG, "setLoopRecordTime time = " + time);
        mLoopRecordingTime = time * 1000 * 1000;
    }

    /**
     * 设置录制单个非循环视得时长,单位：秒
     *
     * @param time
     */
    public synchronized void setRecordTime(int time) {
        BYDLog.d(TAG, "setRecordTime time = " + time);
        mRecordingTime = time * 1000 * 1000;
    }

    /**
     * 增加非循环视频单个视频录制时长
     *
     * @param time
     */
    public synchronized void addRecordTime(int time) {
        BYDLog.d(TAG, "addRecordTime time = " + time);
        mRecordingTime += time * 1000 * 1000;
        BYDLog.d(TAG, "addRecordTime mRecordingTime = " + mRecordingTime);
    }

    /**
     * 在当前非循环视频已录制时间基础上延长指定时间, 单位：秒
     */
    public synchronized void appendRecordTime(long time) {
        BYDLog.d(TAG, "appendRecordTime time = " + time);
        mRecordingTime = getAlreadRecordTime() + time * 1000 * 1000;
        BYDLog.d(TAG, "addRecordTime mRecordingTime = " + mRecordingTime);
    }

    /**
     * 获取非循环视频已录制时长，单位:秒
     *
     * @return
     */
    public long getAlreadRecordTime() {
        long alreadRecodTime = 0;
        FileWriterRunnable fileWriterRunnable = getFileWriterRunnable(FileSwapHelper.LOCK);
        if (fileWriterRunnable != null) {
            alreadRecodTime = fileWriterRunnable.getAlreadyRecordTime();
        }
        return alreadRecodTime / 1000 / 1000;
    }

    /**
     * 获取循环视频录制总时长 ， 单位: 秒
     *
     * @return
     */
    public long getRecordingTime() {
        BYDLog.d(TAG, "getRecordingTime recordingTime = " + mRecordingTime);
        return mRecordingTime;
    }

    /**
     * 获取循环视频录制总时长 ， 单位: 秒
     *
     * @return
     */
    public long getloopRecordingTime() {
        BYDLog.d(TAG, "getloopRecordingTime mLoopRecordingTime = " + mLoopRecordingTime);
        return mLoopRecordingTime;
    }


    public void setRecorverEnable(boolean isEnable) {
        BYDLog.d(TAG, "setRecorverEnable: isEnable = " + isEnable);
        mRecorverEnable = isEnable;
    }

    private FileWriterRunnable getFileWriterRunnable(String type) {
        for (Map.Entry<String, FileWriterRunnable> entry : fileWriterRunnableMap.entrySet()) {
            if (entry.getKey().equals(type)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 无预览编码开始后添加时间水印
     *
     * @param watermarkTimeInfo
     */
    public void addWatermarkTime(WatermarkTimeInfo watermarkTimeInfo) {
        if (mWatermarkRender != null) {
            mWatermarkRender.addWatermarkTime(watermarkTimeInfo);
        } else {
            BYDLog.e(TAG, "recorder is not start! ");
        }
    }

    /**
     * 无预览编码开始后添加图片水印
     *
     * @param watermarkBitmapInfo
     */
    public void addWatermarkBitmap(WatermarkBitmapInfo watermarkBitmapInfo) {
        if (mWatermarkRender != null) {
            mWatermarkRender.addWatermarkBitmap(watermarkBitmapInfo);
        } else {
            BYDLog.e(TAG, "recorder is not start! ");
        }
    }

    /**
     * 无预览编码开始后添加文字水印
     *
     * @param watermarkTextInfo
     */
    public void addWatermarkText(WatermarkTextInfo watermarkTextInfo) {
        if (mWatermarkRender != null) {
            mWatermarkRender.addWatermarkText(watermarkTextInfo);
        } else {
            BYDLog.e(TAG, "recorder is not start! ");
        }
    }

    /**
     * 无预览拍照
     *
     * @return
     */
    public boolean takingPhoto() {
        if (videoEncodeRecode != null) {
            return videoEncodeRecode.takingPhoto();
        }
        return false;
    }

    public Context getContext() {
        if (mContext == null) {
            try {
                throw new Exception("The SDK has not been initialized yet！");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mContext;
    }

    public void registerVideoStateListener(VideoStateListener videoStateListener) {
        if (!videoStateListeners.contains(videoStateListener)) {
            videoStateListeners.add(videoStateListener);
        }
    }

    public MediaFormat getVideoMediaFormat() {
        return videoMediaFormat;
    }

    @Override
    public void onMediaFormateChanged(MediaFormat mediaFormat) {
        BYDLog.d(TAG, "onMediaFormateChanged: " + mediaFormat + "    " + fileWriterRunnableMap.size());
        videoMediaFormat = mediaFormat;
        for (Map.Entry<String, FileWriterRunnable> entry : fileWriterRunnableMap.entrySet()) {
            FileWriterRunnable fileWriterRunnable = entry.getValue();
            fileWriterRunnable.setMediaFormat(mediaFormat);
        }
    }

    @Override
    public void onMediaDataCodec(VideoCodeEncoder.MediaCodecData data) {
        for (Map.Entry<String, FileWriterRunnable> entry : fileWriterRunnableMap.entrySet()) {
            FileWriterRunnable fileWriterRunnable = entry.getValue();
            fileWriterRunnable.addData(data);
        }
    }

    @Override
    public void onNotify(int msg, int ext1, int ext2, Object obj) {
        BYDLog.d(TAG, "onNotify:  msg = " + SentryModeHandlerMsg.getMessageName(msg));
        switch (msg) {
            case SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD_VIDEO_END:
            case SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD_VIDEO_START:
                String videoName = (String) obj;
                boolean isLoop = videoName.contains(FileSwapHelper.NORMAL);
                int state = msg == SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD_VIDEO_END ? VideoStateListener.VIDEO_STOP : VideoStateListener.VIDEO_START;
                BYDLog.d(TAG, "onNotify:  isLoop = " + isLoop);
                BYDLog.d(TAG, "onNotify:  videoName = " + videoName);
                BYDLog.d(TAG, "onNotify:  state = " + state);
                for (VideoStateListener v : videoStateListeners) {
                    v.onVideoStatusChange(isLoop, videoName, state);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isCallback(int msg, int type) {
        return SentryModeHandler.isCallback(msg, type);
    }
}
