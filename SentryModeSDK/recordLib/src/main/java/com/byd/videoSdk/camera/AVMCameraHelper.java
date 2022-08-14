package com.byd.videoSdk.camera;

import android.graphics.SurfaceTexture;

public class AVMCameraHelper implements ICameraHelper {
    @Override
    public int getPreviewWidth() {
        return 0;
    }

    @Override
    public int getPreviewHeight() {
        return 0;
    }

    @Override
    public void openCamera(int cameraId) {

    }

    @Override
    public void startPreview( int width, int height) {

    }

    @Override
    public void setDisplay(SurfaceTexture surfaceTexture) {

    }

    @Override
    public boolean isPreview() {
        return false;
    }

    @Override
    public int getDefaultCameraId() {
        return 0;
    }

    @Override
    public void releaseCamera() {

    }
}
