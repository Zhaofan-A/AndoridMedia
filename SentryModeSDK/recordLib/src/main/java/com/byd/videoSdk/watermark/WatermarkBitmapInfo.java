package com.byd.videoSdk.watermark;

import android.graphics.Bitmap;

import java.nio.FloatBuffer;

public class WatermarkBitmapInfo extends WatermarkBaseInfo{
    private static final String TAG = "WatermarkBitmapInfo";

    public boolean isInitData = false;

    private int position_x;
    private int position_y;
    public Bitmap bitmap;
    public int waterTextureId  = -1;
    public int vboId = -1;

    //位置
    public FloatBuffer vertexBuffers ;


    /**
     * 图片水印构造方法
     * @param bitmap         水印要显示得图片
     * @param startX     水印图片相对于屏幕上的X轴起始位置
     * @param startY     水印图片相对于屏幕上的Y轴起始位置
     */
    public WatermarkBitmapInfo( Bitmap bitmap , int startX, int startY){
        this.bitmap = bitmap;
        this.position_x = startX;
        this.position_y = startY;
        createVertexBuffer();
    }

    @Override
    public void createVertexBuffer() {
//        Point point = calculateWaterXY(position_x, position_y, bitmap.getWidth(), bitmap.getHeight());
//        initData(waterVertexData, point);
        initWaterVertexData(position_x, position_y, bitmap.getWidth(), bitmap.getHeight(),waterVertexData);
        vertexBuffers = createFloatBuffer(waterVertexData);
    }


    public void release(){
        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
        }
    }

    @Override
    public String toString() {
        return "WatermarkBitmapInfo{" +
                "bitmap=" + bitmap +
                ", position_x=" + position_x +
                ", position_y=" + position_y +
                '}';
    }


}
