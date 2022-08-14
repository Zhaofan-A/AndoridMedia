package com.byd.videoSdk.recorder.encodec;

import android.content.Context;

import com.byd.videoSdk.egl.EglHelper;
import com.byd.videoSdk.interfaces.IWatermarkRender;
import com.byd.videoSdk.render.VideoEncodeRender;


public class VideoEGLThread extends Thread{

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_WHEN_DIRTY;

    private VideoEncodeRender mRender;
    private VideoCodeEncoder codeEncoder;
    private EglHelper eglHelper;
    private Object object;
    private boolean isExit = false;
    public boolean isCreate = false;
    public boolean isChange = false;
    public boolean isStart = false;


    public VideoEGLThread(Context context, VideoCodeEncoder codeEncoder) {
        this.codeEncoder = codeEncoder;
        mRender = new VideoEncodeRender(context, codeEncoder.getTextureId());
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    private void setRenderMode(int mRenderMode) {
        if (mRender == null) {
            throw new RuntimeException("must set render before");
        }
        this.mRenderMode = mRenderMode;
    }

    public void setWatermarkRender(IWatermarkRender watermarkRender){
        mRender.setWatermarkRender(watermarkRender);
    }

    public boolean takingPhoto() {
       return mRender.takingPhoto();
    }

    @Override
    public void run() {
        super.run();
        isExit = false;
        isStart = false;
        object = new Object();
        eglHelper = new EglHelper();
        eglHelper.initEgl(codeEncoder.getSurface(), codeEncoder.getEGLContext());

        while (true) {
            try {
                if (isExit) {
                    release();
                    break;
                }
                if (isStart) {
                    if (mRenderMode == RENDERMODE_WHEN_DIRTY) {
                        synchronized (object) {
                            object.wait();
                        }
                    } else if (mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        Thread.sleep(1000 / 60);
                    } else {
                        throw new IllegalArgumentException("renderMode");
                    }
                }

                onCreate();
                onChange(codeEncoder.getEncodeWidth(), codeEncoder.getEncodeHeight());
                onDraw();
                isStart = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mRender.onDestory();
        mRender.onRelease();
    }

    private void onCreate() {
        if (!isCreate || mRender == null)
            return;

        isCreate = false;
        mRender.onSurfaceCreated();
    }

    private void onChange(int width, int height) {
        if (!isChange || mRender == null)
            return;

        isChange = false;
        mRender.onSurfaceChanged(width, height);
    }

    private void onDraw() {
        if (mRender == null)
            return;

        mRender.onDrawFrame();
        eglHelper.swapBuffers();
    }

    public  void requestRender() {
        if (object != null) {
            synchronized (object) {
                object.notifyAll();
            }
        }
    }

    public void onDestroy() {
        isExit = true;
        //释放锁
        requestRender();
    }


    public void release() {
        if (eglHelper != null) {
            eglHelper.destoryEgl();
            eglHelper = null;
            object = null;
            codeEncoder = null;
        }
    }


}
