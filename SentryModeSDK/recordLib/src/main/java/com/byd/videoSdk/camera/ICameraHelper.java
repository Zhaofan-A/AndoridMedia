package com.byd.videoSdk.camera;

import android.app.Activity;
import android.graphics.SurfaceTexture;

public interface ICameraHelper {
    int getPreviewWidth();
    int getPreviewHeight();
    void openCamera(int cameraId);
    void startPreview(int width, int height);
    void setDisplay(SurfaceTexture surfaceTexture);
    boolean isPreview();
    int getDefaultCameraId();
    void releaseCamera();
}
