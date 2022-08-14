package com.byd.videoSdk.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;


import com.byd.videoSdk.common.util.DisplayUtil;

import java.io.IOException;
import java.util.List;

public class CameraHelper implements ICameraHelper{
    private Camera camera;
    private static final String TAG = "CameraHelper";

    private static int mDefaultCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int screenW, screenH;
    private int PreviewWidth;
    private int PreviewHeight;

    public CameraHelper(Context context) {
        screenW = DisplayUtil.getScreenW(context);
        screenH = DisplayUtil.getScreenH(context);
    }


    public void autoFocus() {
        if (camera != null) {
            camera.autoFocus(null);
        }
    }

    /**
     * 照相最佳的分辨率
     *
     * @param sizes
     * @return
     */
    private Camera.Size findBestSizeValue(List<Camera.Size> sizes, int w, int h, double minDiff) {

        //摄像头这个size里面都是w > h
        if (w < h) {
            int t = h;
            h = w;
            w = t;
        }

        double targetRatio = (double) w / h;
        Log.e(TAG, "照相尺寸  w:" + w + "  h:" + h + "  targetRatio:" + targetRatio + "  minDiff:" + minDiff);
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;

            double diff = Math.abs(ratio - targetRatio);

            Log.e(TAG, "照相支持尺寸  width:" + size.width + "  height:" + size.height + "  targetRatio:" + targetRatio + "" +
                    "  ratio:" + ratio + "   diff:" + diff);

            if (diff > minDiff) {
                continue;
            }

            if (optimalSize == null) {
                optimalSize = size;
            } else {
                if (optimalSize.width * optimalSize.height < size.width * size.height) {
                    optimalSize = size;
                }
            }
            minDiff = diff;
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff += 0.1f;
            if (minDiff > 1.0f) {
                optimalSize = sizes.get(0);
            } else {
                optimalSize = findBestSizeValue(sizes, w, h, minDiff);
            }
        }
        if (optimalSize != null)
            Log.e(TAG, "照相best尺寸  " + optimalSize.width + "  " + optimalSize.height);
        return optimalSize;

    }


    public int getPreviewWidth() {
        return PreviewWidth;
    }

    public int getPreviewHeight() {
        return PreviewHeight;
    }

    @Override
    public void openCamera(int cameraId) {
        Log.e(TAG, "openCamera  camera = "+camera);
        if(camera == null){
            camera = Camera.open(cameraId);
        }else{
            Log.e(TAG, "camera is already open");
        }
    }

    @Override
    public void startPreview(int width, int height) {

        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        parameters.setPreviewFormat(ImageFormat.NV21);

        //设置对焦模式
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        Camera.Size size = findBestSizeValue(parameters.getSupportedPreviewSizes(), screenW, screenH, 0.1f);
        parameters.setPreviewSize(size.width, size.height);
        PreviewWidth = size.width;
        PreviewHeight = size.height;

        Log.d(TAG, "startPreview: "+PreviewWidth);

        size = findBestSizeValue(parameters.getSupportedPictureSizes(), screenW, screenH, 0.1f);
        parameters.setPictureSize(size.width, size.height);

        camera.setParameters(parameters);
        camera.startPreview();

        autoFocus();
    }

    @Override
    public void setDisplay(SurfaceTexture surfaceTexture) {
        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isPreview() {
        if(camera != null && PreviewWidth != 0){
               return  true;
        }
        return false;
    }

    @Override
    public int getDefaultCameraId() {
        return mDefaultCameraID;
    }

    @Override
    public void releaseCamera() {
        Log.d(TAG, "releaseCamera: "+camera);
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
