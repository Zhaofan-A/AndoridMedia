package com.byd.videoSdk.watermark;

import android.util.Log;

import com.byd.videoSdk.common.util.BYDLog;
import com.byd.videoSdk.common.util.DisplayUtil;
import com.byd.videoSdk.recorder.RecorderController;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class WatermarkBaseInfo  {
    private static final String TAG = "WatermarkBaseInfo";

     float[] waterVertexData = {
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f
    };

    public WatermarkBaseInfo(){
    }

    public abstract void createVertexBuffer();


   public  void  initWaterVertexData(int x, int y, int w, int h ,float[] waterVertexData ){
       int width = DisplayUtil.getScreenW(RecorderController.getInstance().getContext());
       int height = DisplayUtil.getScreenH(RecorderController.getInstance().getContext());

       NativeLibrary.getInstance().initWatermark();
       String jniStr = NativeLibrary.getInstance().stringFromJNI();
       NativeLibrary.getInstance().initWaterVertexData(x,y,w,h,width,height,waterVertexData);
       BYDLog.d(TAG, "jniStr = " + jniStr);
   }


    public  Point calculateWaterXY(int x, int y, int w, int h) {
        int width = DisplayUtil.getScreenW(RecorderController.getInstance().getContext());
        int height = DisplayUtil.getScreenH(RecorderController.getInstance().getContext());
//        int width = Constants.IMAGE_WIDTH;
//        int height = Constants.IMAGE_HEIGHT;

        Log.d(TAG, "calculateWaterXY: width = " + width + "  height = " + height);
        float pW = width / 2.0f, pH = height / 2.0f;   //x轴分为两侧（x每侧占width比例），y轴分为两侧
        Point point = new Point();
        if (x > pW) {
            point.x = 1.0f / pW * (x - pW);
        } else {
            point.x = -1 * (1.0f - 1.0f / pW * x);
        }
        if (y > pH) {
            point.y = -1.0f / pH * (y - pH);
        } else {
            point.y = 1 - 1.0f / pH * y;
        }

        point.w = 1.0f / pW * w;
        point.h = 1.0f / pH * h;
        return point;
    }

    public  void initData(float[] vertexData, Point point) {
        Log.d(TAG, "initData: " + point.toString());
        vertexData[0] = point.x;
        vertexData[1] = point.y;
        vertexData[2] = 0;

        vertexData[3] = point.x + point.w;
        vertexData[4] = point.y;
        vertexData[5] = 0;

        vertexData[6] = point.x;
        vertexData[7] = point.y + point.h;
        vertexData[8] = 0;

        vertexData[9] = point.x + point.w;
        vertexData[10] = point.y + point.h;
        vertexData[11] = 0;
    }


    public FloatBuffer createFloatBuffer(float[] datas) {
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(datas.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(datas);
        floatBuffer.position(0);
        return floatBuffer;
    }

    protected class Point {
        float x;
        float y;
        float w;
        float h;

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    ", w=" + w +
                    ", h=" + h +
                    '}';
        }
    }
}
