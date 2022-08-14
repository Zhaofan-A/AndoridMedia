package com.byd.videoSdk.recorder.encodec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.byd.videoSdk.common.util.BYDLog;
import com.byd.videoSdk.interfaces.IWatermarkRender;
import com.byd.videoSdk.recorder.utils.SDKMediaFormat;
import com.byd.videoSdk.watermark.WatermarkBitmapInfo;
import com.byd.videoSdk.watermark.WatermarkTextInfo;
import com.byd.videoSdk.watermark.WatermarkTimeInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLContext;


public class VideoCodeEncoder {
    private static final String TAG = "VideoCodeEncoder";

    private Surface mSurface;
    private EGLContext mEGLContext;

    private MediaCodec.BufferInfo mVideoBuffInfo;
    private MediaCodec mVideoEncodec;
    private int width, height;

    private VideoEncodecThread mVideoEncodecThread;
    private VideoEGLThread mVideoEGLThread;

    private Context context;
    private int textureId;

    ArrayList<OnVideoCodeCallback> mOnVideoCodeCallback = new ArrayList<OnVideoCodeCallback>();

    public VideoCodeEncoder(Context context) {
        this.context = context;
    }

    public void startRecode() {
        if (mSurface != null && mEGLContext != null) {
            mVideoEncodecThread = new VideoEncodecThread(this);
            mVideoEGLThread = new VideoEGLThread(context, this);
            mVideoEGLThread.isCreate = true;
            mVideoEGLThread.isChange = true;
            mVideoEGLThread.start();
            mVideoEncodecThread.start();
        }
        BYDLog.d(TAG, "startRecode: ");
    }

    public void stopRecode() {
        BYDLog.d(TAG, "stopRecode: start !");
        if (mVideoEncodecThread != null) {
            mVideoEncodecThread.exit();
            mVideoEncodecThread = null;
        }

        if (mVideoEGLThread != null) {
            mVideoEGLThread.onDestroy();
            mVideoEGLThread = null;
        }
        BYDLog.d(TAG, "stopRecode: end !");
    }

    public void initEncoder(EGLContext eglContext, int textureId, int width, int height) {
        this.width = width;
        this.height = height;
        this.mEGLContext = eglContext;
        this.textureId = textureId;
        initVideoEncoder(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
    }

    public Surface getSurface() {
        return mSurface;
    }

    public EGLContext getEGLContext() {
        return mEGLContext;
    }

    public int getTextureId() {
        return textureId;
    }

    public int getEncodeWidth() {
        return width;
    }

    public int getEncodeHeight() {
        return height;
    }

    public boolean isEncodeStart() {
        return mVideoEncodecThread != null;
    }


    public void setWatermarkRender(IWatermarkRender watermarkRender) {
        if(mVideoEGLThread != null){
            mVideoEGLThread.setWatermarkRender(watermarkRender);
        }
    }

    public boolean takingPhoto() {
       return mVideoEGLThread.takingPhoto();
    }

    public MediaCodec getMediaCodec(){
        return mVideoEncodec;
    }

    public MediaCodec.BufferInfo getMediaCodecBufferInfo(){
        return mVideoBuffInfo;
    }

    private void initVideoEncoder(String mineType, int width, int height) {
        try {
              mVideoEncodec = MediaCodec.createEncoderByType(mineType);
//            MediaFormat videoFormat = MediaFormat.createVideoFormat(mineType, width, height);
//            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, Constants.FRAME_RATE);
//            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, Constants.BIT_RATE);//width * height * 4 RGBA
//            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Constants.I_FRAME_INTERVAL);
//            Log.d(TAG, "initVideoEncoder: "+width+"  "+height);
//            //设置压缩等级  默认是baseline  Profile是对视频压缩特性的描述（CABAC呀、颜色采样数等等）。
//            //Level是对视频本身特性的描述（码率、分辨率、fps）。
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                videoFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileMain);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    videoFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3);
//                }
//            }
            mVideoEncodec.reset();
            MediaFormat videoFormat = SDKMediaFormat.getMPEG4MediaFormat();
            BYDLog.d(TAG, "initVideoEncoder: videoFormat = "+videoFormat);
            mVideoEncodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mVideoBuffInfo = new MediaCodec.BufferInfo();
            mSurface = mVideoEncodec.createInputSurface();
        } catch (IOException e) {
            e.printStackTrace();
            mVideoEncodec = null;
            mVideoBuffInfo = null;
            mSurface = null;
        }
    }

    protected synchronized void callbackMediaFormat(MediaFormat mediaFormat) {
        if (mOnVideoCodeCallback.size() > 0) {
            for (OnVideoCodeCallback cb : mOnVideoCodeCallback) {
                if (cb != null) {
                    cb.onMediaFormateChanged(mediaFormat);
                }
            }
        }
    }

    protected synchronized void callbackMuxerData(MediaCodecData data) {
        if (mOnVideoCodeCallback.size() > 0) {
            if (data != null) {
                ByteBuffer bytebuffer = ByteBuffer.allocateDirect(data.bufferInfo.size); //通过操作系统来创建内存块用作缓冲区
                bytebuffer.put(data.byteBuf);
                MediaCodec.BufferInfo bufInfo = new MediaCodec.BufferInfo();
                bufInfo.set(data.bufferInfo.offset,
                        data.bufferInfo.size,
                        data.bufferInfo.presentationTimeUs,  //缓冲区的表示时间戳，以微秒为单位
                        data.bufferInfo.flags);
                MediaCodecData data1 = new MediaCodecData(data.trackIndex,
                        bytebuffer,
                        bufInfo);
                for (OnVideoCodeCallback cb : mOnVideoCodeCallback) {
                    if (cb != null) {
                        cb.onMediaDataCodec(data1);
                    }
                }
            }
        }
    }

    /**
     * Unregister the callback.
     *
     * @param cb
     */
    public synchronized void unregisterCallback(OnVideoCodeCallback cb) {
        if (mOnVideoCodeCallback.contains(cb)) {
            mOnVideoCodeCallback.remove(cb);
        }
    }

    /**
     * Register callback to receive the codec data
     *
     * @param cb
     */
    public synchronized void registerCallback(OnVideoCodeCallback cb) {
        BYDLog.d(TAG, "registerCallback");
        if (!mOnVideoCodeCallback.contains(cb)) {
            mOnVideoCodeCallback.add(cb);
        }
    }


    public interface OnVideoCodeCallback {
        void onMediaFormateChanged(MediaFormat mediaFormat);

        void onMediaDataCodec(MediaCodecData data);
    }

    public static class MediaCodecData {
        public int trackIndex;
        public ByteBuffer byteBuf;
        public MediaCodec.BufferInfo bufferInfo;

        public MediaCodecData(int trackIndex, ByteBuffer teBuf, MediaCodec.BufferInfo bufferInfo) {
            this.trackIndex = trackIndex;
            this.byteBuf = teBuf;
            this.bufferInfo = bufferInfo;
        }
    }


}
