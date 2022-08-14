package com.byd.videoSdk.egl;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.byd.videoSdk.common.util.BYDLog;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

public class EglSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "EglSurfaceView";

    private Render mRenderer;
    private EGLThread mEGLThread;
    private Surface mSurface;
    private EGLContext mEglContext;


    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_WHEN_DIRTY;


    public EglSurfaceView(Context context) {
        this(context, null);
    }

    public EglSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EglSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        BYDLog.d(TAG, "init: ");
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        BYDLog.d(TAG, "surfaceCreated: ");
//        if (mSurface == null) {
            mSurface = holder.getSurface();
//        }
        mEGLThread = new EGLThread(new WeakReference<>(this));
        mEGLThread.isCreate = true;
        mEGLThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        BYDLog.d(TAG, "surfaceChanged: width = "+width +"   height = "+height);
        mEGLThread.width = width;
        mEGLThread.height = height;
        mEGLThread.isChange = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        BYDLog.d(TAG, "surfaceDestroyed: ");
        mEGLThread.onDestroy();
        mEGLThread = null;
        mSurface = null;
        mEglContext = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        BYDLog.d(TAG, "onDetachedFromWindow: ");
        mRenderer.onRelease();
    }


    public void setRender(Render mRenderer) {
        this.mRenderer = mRenderer;
    }

    public void setRenderMode(int renderMode) {
        this.mRenderMode = renderMode;
    }

    public void requestRender() {
        if (mEGLThread != null) {
            mEGLThread.requestRender();
        }
    }

    public void setSurfaceAndEglContext(Surface surface, EGLContext eglContext) {
        this.mSurface = surface;
        this.mEglContext = eglContext;
    }

    public EGLContext getEglContext() {
        if (mEGLThread != null) {
            return mEGLThread.getEglContext();
        }
        return null;
    }


    private static class EGLThread extends Thread {

        EGLThread(WeakReference<EglSurfaceView> eGLSurfaceViewWeakRef) {
            this.mEGLSurfaceViewWeakRef = eGLSurfaceViewWeakRef;
        }

        @Override
        public void run() {
            super.run();
            try {
                guardedRun();
            } catch (Exception e) {
                // fall thru and exit normally
            }
        }

        private void guardedRun() throws InterruptedException {
            isExit = false;
            isStart = false;
            object = new Object();
            mEglHelper = new EglHelper();
            mEglHelper.initEgl(mEGLSurfaceViewWeakRef.get().mSurface, mEGLSurfaceViewWeakRef.get().mEglContext);

            while (true) {
                if (isExit) {
                    //释放资源
                    release();
                    break;
                }

                if (isStart) {
                    if (mEGLSurfaceViewWeakRef.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                        synchronized (object) {
                            object.wait();
                        }
                    } else if (mEGLSurfaceViewWeakRef.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        Thread.sleep(1000 / 60);
                    } else {
                        throw new IllegalArgumentException("renderMode");
                    }
                }

                onCreate();
                onChange(width, height);
                onDraw();
                isStart = true;
            }
            BYDLog.d(TAG, "guardedRun: onDestory");
            mEGLSurfaceViewWeakRef.get().mRenderer.onDestory();
        }

        private void onCreate() {
            if (!isCreate || mEGLSurfaceViewWeakRef.get().mRenderer == null)
                return;

            isCreate = false;
            mEGLSurfaceViewWeakRef.get().mRenderer.onSurfaceCreated();
        }


        private void onChange(int width, int height) {
            if (!isChange || mEGLSurfaceViewWeakRef.get().mRenderer == null)
                return;

            isChange = false;
            mEGLSurfaceViewWeakRef.get().mRenderer.onSurfaceChanged(width, height);
        }

        private void onDraw() {
            if (mEGLSurfaceViewWeakRef.get().mRenderer == null)
                return;

            mEGLSurfaceViewWeakRef.get().mRenderer.onDrawFrame();

            mEglHelper.swapBuffers();
        }

        void requestRender() {
            if (object != null) {
                synchronized (object) {
                    object.notifyAll();
                }
            }
        }

        void onDestroy() {
            isExit = true;
            //释放锁
            requestRender();
        }

        void release() {
            if (mEglHelper != null) {
                mEglHelper.destoryEgl();
                mEglHelper = null;
                object = null;
                mEGLSurfaceViewWeakRef = null;
            }
        }

        EGLContext getEglContext() {
            if (mEglHelper != null) {
                return mEglHelper.getEglContext();
            }
            return null;
        }

        private WeakReference<EglSurfaceView> mEGLSurfaceViewWeakRef;
        private EglHelper mEglHelper;

        private int width;
        private int height;

        private boolean isCreate;
        private boolean isChange;
        private boolean isStart;
        private boolean isExit;

        private Object object;
    }


    public interface Render {
        void onSurfaceCreated();

        void onSurfaceChanged(int width, int height);

        void onDrawFrame();

        void onDestory();

        void onRelease();
    }
}
