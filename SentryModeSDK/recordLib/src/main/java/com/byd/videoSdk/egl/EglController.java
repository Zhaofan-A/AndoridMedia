package com.byd.videoSdk.egl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.byd.videoSdk.camera.CameraHelper;
import com.byd.videoSdk.camera.ICameraHelper;
import com.byd.videoSdk.common.util.BYDLog;
import com.byd.videoSdk.recorder.utils.Constants;
import com.byd.videoSdk.render.CameraFboRender;

import javax.microedition.khronos.egl.EGLContext;

public class EglController implements CameraFboRender.OnSurfaceTextureListener {
    private static final String TAG = "EglController";

    private static EglController eglController;
    private ICameraHelper cameraHelper;

    private CameraFboRender mRenderer;
    private EGLContext mEglContext = null;

    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int textureId;
    private SurfaceTexture surfaceTexture;

    private HandlerThread mEGLHandlerThread;
    private EGLHandler mEGLHandler;
    EglInitListener mEglInitListener;

    private int mWidth;
    private int mHeight;

    private static final int WHAT_INIT = 0x000;
    private static final int WHAT_CREATE = 0x001;
    private static final int WHAT_CHANGE = 0x002;
    public static final int WHAT_SWAP = 0x003;
    public static final int WHAT_OPEN_CAMERA = 0x004;
    public static final int WHAT_MAKE = 0x005;

    private EglController() {
    }

    public static EglController getInstance() {
        if (eglController == null) {
            eglController = new EglController();
        }
        return eglController;
    }

    private void initEgl(Context context) {
        cameraHelper = new CameraHelper(context);

        mRenderer = new CameraFboRender(context, false);
        mRenderer.setOnSurfaceListener(this);
        previewAngle(context);
        mRenderer.setFrameAvailableEnable(true);

        mEGLHandlerThread = new HandlerThread("EGLHandler");
        mEGLHandlerThread.start();
        mEGLHandler = new EGLHandler(mEGLHandlerThread.getLooper());

        mEGLHandler.sendEmptyMessage(WHAT_INIT);
        mEGLHandler.sendEmptyMessageDelayed(WHAT_CREATE, 0);
    }

    private void changeView(int width, int height) {
        mWidth = width;
        mHeight = height;
        mEGLHandler.setWidthHeight(width, height);
        mEGLHandler.sendEmptyMessage(WHAT_CHANGE);
    }

    public void startCamera(Context context) {
        initEgl(context);
        changeView(Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGHT);
        mEGLHandler.removeMessages(WHAT_OPEN_CAMERA);
        mEGLHandler.sendEmptyMessage(WHAT_OPEN_CAMERA);
    }


    public void sendMessagToEGLHandler(int what) {
        mEGLHandler.sendEmptyMessage(what);
    }

    public EGLHandler getHandler() {
        return mEGLHandler;
    }

    public void release() {
        BYDLog.d(TAG, "release done!");
        if (cameraHelper != null) {
            cameraHelper.releaseCamera();
            cameraHelper = null;
        }
        if (mEGLHandler != null) {
            mEGLHandler.release();
        }
        if (mEGLHandlerThread != null && mEGLHandlerThread.isAlive()) {
            mEGLHandlerThread.quitSafely();
            BYDLog.d(TAG, "release wait!");
            try {
                mEGLHandlerThread.join(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mEGLHandlerThread = null;
            mEGLHandler = null;
            BYDLog.d(TAG, "release done!");
        }
    }

    public int getTextureId() {
        return textureId;
    }

    public EGLContext getEglContext() {
        if (mEGLHandler != null) {
            return mEGLHandler.getEglContext();
        }
        return null;
    }

    @Override
    public void onSurfaceTextureCreate(SurfaceTexture surfaceTexture, int textureId) {
        this.surfaceTexture = surfaceTexture;
        this.textureId = textureId;
        if (mEglInitListener != null) {
            mEglInitListener.onEglCreated();
        }
    }

    private static class EGLHandler extends Handler {

        private EglHelper mEglHelper;
        private int width;
        private int height;

        EGLHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            BYDLog.d(TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case WHAT_INIT:
                    mEglHelper = new EglHelper();
                    mEglHelper.initEgl(null, getInstance().mEglContext);
                    break;
                case WHAT_CREATE:
                    onCreate();
                    break;
                case WHAT_CHANGE:
                    onChange(width, height);
                    break;
                case WHAT_SWAP:
                    swapBuffers();
                    break;
                case WHAT_OPEN_CAMERA:
                    if (getInstance().surfaceTexture != null && !getInstance().cameraHelper.isPreview()) {
                        getInstance().cameraHelper.openCamera(getInstance().cameraId);
                        getInstance().cameraHelper.setDisplay(getInstance().surfaceTexture);
                        getInstance().cameraHelper.startPreview(getInstance().mWidth, getInstance().mHeight);
                    } else {
                        sendEmptyMessageDelayed(WHAT_OPEN_CAMERA, 500);
                    }
                    break;
                case WHAT_MAKE:
                    break;
                default:
                    break;
            }
        }

        public void setWidthHeight(int width, int height) {
            this.width = width;
            this.height = height;
        }

        private void onCreate() {
            if (getInstance().mRenderer == null)
                return;
            getInstance().mRenderer.onSurfaceCreated();
        }

        private void onChange(int width, int height) {
            if (getInstance().mRenderer == null)
                return;
            getInstance().mRenderer.onSurfaceChanged(width, height);
        }

        void swapBuffers() {
            if (mEglHelper != null) {
                boolean result = mEglHelper.swapBuffers();
                BYDLog.d(TAG, "swapBuffers: result = " + result);
            }
        }

        void release() {
            if (mEglHelper != null) {
                mEglHelper.destoryEgl();
                mEglHelper = null;
            }
        }

        EGLContext getEglContext() {
            if (mEglHelper != null) {
                return mEglHelper.getEglContext();
            }
            return null;
        }

    }


    public int getCameraPrivewWidth() {
        return cameraHelper.getPreviewWidth();
    }

    public int getCameraPrivewHeight() {
        return cameraHelper.getPreviewHeight();
    }

    public void previewAngle(Context context) {
        int angle = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        mRenderer.resetMatirx();
        switch (angle) {
            case Surface.ROTATION_0:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mRenderer.setAngle(90, 0, 0, 1);
                    mRenderer.setAngle(180, 1, 0, 0);
                } else {
                    mRenderer.setAngle(90f, 0f, 0f, 1f);
                }

                break;
            case Surface.ROTATION_90:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mRenderer.setAngle(180, 0, 0, 1);
                    mRenderer.setAngle(180, 0, 1, 0);
                } else {
                    mRenderer.setAngle(90f, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_180:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mRenderer.setAngle(90f, 0.0f, 0f, 1f);
                    mRenderer.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    mRenderer.setAngle(-90, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_270:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mRenderer.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    mRenderer.setAngle(0f, 0f, 0f, 1f);
                }
                break;
        }
    }

    public void setEglInitListener(EglInitListener eglInitListener) {
        mEglInitListener = eglInitListener;
    }

    public interface EglInitListener {
        void onEglCreated();
    }
}
