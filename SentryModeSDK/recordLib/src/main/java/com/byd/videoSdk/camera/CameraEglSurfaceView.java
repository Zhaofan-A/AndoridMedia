package com.byd.videoSdk.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.byd.videoSdk.egl.EglSurfaceView;
import com.byd.videoSdk.render.CameraFboRender;
import com.byd.videoSdk.watermark.WatermarkBitmapInfo;
import com.byd.videoSdk.watermark.WatermarkFactor;
import com.byd.videoSdk.watermark.WatermarkRender;
import com.byd.videoSdk.watermark.WatermarkTextInfo;
import com.byd.videoSdk.watermark.WatermarkTimeInfo;


public class CameraEglSurfaceView extends EglSurfaceView implements CameraFboRender.OnSurfaceTextureListener {
    private static final String TAG = "CameraEglSurfaceView";

    private ICameraHelper cameraHelper;
    private CameraFboRender render;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int textureId;
    private WatermarkRender mWatermarkRender;

    public CameraEglSurfaceView(Context context) {
        this(context, null);
    }

    public CameraEglSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraEglSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        cameraHelper = new CameraHelper(context);
        render = new CameraFboRender(context, true);
        render.setOnSurfaceListener(this);
        setRender(render);
        previewAngle(context);

        mWatermarkRender = WatermarkFactor.getInstance(context).getWatermarkRender(WatermarkFactor.PREVIEW_WATERMARK_RENDER);
    }

    public int getCameraPrivewWidth() {
        return cameraHelper.getPreviewWidth();
    }

    public int getCameraPrivewHeight() {
        return cameraHelper.getPreviewHeight();
    }

    @Override
    public void onSurfaceTextureCreate(SurfaceTexture surfaceTexture, int textureId) {
        cameraHelper.openCamera(cameraId);
        cameraHelper.setDisplay(surfaceTexture);
        cameraHelper.startPreview(this.getWidth(), this.getHeight());
        this.textureId = textureId;
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (cameraHelper != null) {
            cameraHelper.releaseCamera();
        }
        if(render != null){
            render.onDestory();
        }
    }

    public boolean takingPhoto() {
        return  render.takingPhoto();
    }

    public void addWatermarkTime(WatermarkTimeInfo watermarkTimeInfo) {
        mWatermarkRender.addWatermarkTime(watermarkTimeInfo);
        render.setWatermarkRender(mWatermarkRender);
    }

    public void addWatermarkBitmap(WatermarkBitmapInfo watermarkBitmapInfo) {
        mWatermarkRender.addWatermarkBitmap(watermarkBitmapInfo);
        render.setWatermarkRender(mWatermarkRender);
    }

    public void addWatermarkText(WatermarkTextInfo watermarkTextInfo) {
        mWatermarkRender.addWatermarkText(watermarkTextInfo);
        render.setWatermarkRender(mWatermarkRender);
    }

    public void previewAngle(Context context) {
        int angle = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        render.resetMatirx();
        switch (angle) {
            case Surface.ROTATION_0:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    render.setAngle(90, 0, 0, 1);
                    render.setAngle(180, 1, 0, 0);
                } else {
                    render.setAngle(90f, 0f, 0f, 1f);
                }

                break;
            case Surface.ROTATION_90:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    render.setAngle(180, 0, 0, 1);
                    render.setAngle(180, 0, 1, 0);
                } else {
                    render.setAngle(90f, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_180:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    render.setAngle(90f, 0.0f, 0f, 1f);
                    render.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    render.setAngle(-90, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_270:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    render.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    render.setAngle(0f, 0f, 0f, 1f);
                }
                break;
        }
    }

    public int getTextureId() {
        return textureId;
    }
}
