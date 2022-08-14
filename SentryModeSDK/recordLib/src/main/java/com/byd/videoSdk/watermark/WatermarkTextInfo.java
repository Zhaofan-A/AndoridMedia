package com.byd.videoSdk.watermark;

import android.graphics.Bitmap;

import com.byd.videoSdk.common.util.ShaderUtil;

import java.nio.FloatBuffer;

public class WatermarkTextInfo extends WatermarkBaseInfo {
    private static final String TAG = "WatermarkTextInfo";

    private static String defaultTextColor = "#111111";
    private static String defaultBgColor = "#00000000";
    private static int defaultTextSize = 60;

    public boolean isInitData = false;

    private String text;
    private String textColor;
    private String bgColor;
    private int textSize;
    private int position_x;
    private int position_y;
    public Bitmap bitmap;
    public int waterTextureId;
    public int vboId;

    //位置
    public FloatBuffer vertexBuffers;

    /**
     * 文字水印构造方法
     *
     * @param text       水印要显示得文字
     * @param startX 水印文字相对于屏幕上的X轴起始位置
     * @param startY 水印文字相对于屏幕上的Y轴起始位置
     */
    public WatermarkTextInfo(String text, int startX, int startY) {
        this(text, defaultTextSize, defaultTextColor, defaultBgColor, startX, startY);
    }

    public WatermarkTextInfo(String text, int textSize, String textColor, String bgColor, int position_x, int position_y) {
        this.text = text;
        this.textSize = textSize;
        this.textColor = textColor;
        this.bgColor = bgColor;
        this.position_x = position_x;
        this.position_y = position_y;
        createVertexBuffer();
    }

    public void setTextConfig(int textSize, String colorResId, String bgColorResId) {
        this.textSize = textSize;
        this.textColor = colorResId;
        this.bgColor = bgColorResId;
    }

    public void createVertexBuffer() {
        bitmap = ShaderUtil.createTextImage(text, textSize, textColor, bgColor, 0);
//        Point point = calculateWaterXY(position_x, position_y, bitmap.getWidth(), bitmap.getHeight());
//        initData(waterVertexData, point);
        initWaterVertexData(position_x, position_y, bitmap.getWidth(), bitmap.getHeight(),waterVertexData);
        vertexBuffers = createFloatBuffer(waterVertexData);
    }

    public void release() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    @Override
    public String toString() {
        return "WatermarkTextInfo{" +
                "text=" + text +
                ", position_x=" + position_x +
                ", position_y=" + position_y +
                ", textSize=" + textSize +
                ", textColor=" + textColor +
                '}';
    }

}
