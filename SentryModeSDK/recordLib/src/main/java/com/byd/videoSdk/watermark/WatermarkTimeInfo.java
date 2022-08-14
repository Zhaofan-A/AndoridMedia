package com.byd.videoSdk.watermark;

import android.graphics.Bitmap;
import android.util.Log;

import com.byd.videoSdk.common.util.BYDLog;
import com.byd.videoSdk.common.util.ShaderUtil;

import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WatermarkTimeInfo extends WatermarkBaseInfo {
    private static final String TAG = "WatermarkTimeInfo";

    public final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public boolean isInitData = false;

    private Point[] points = new Point[19];
    public Bitmap[] bitmaps = new Bitmap[13];
    public int[] waterTextureIds = new int[13];
    public int[] vboIds = new int[19];

    //位置
    public FloatBuffer[] vertexBuffers = new FloatBuffer[19];

    private String defaultTextColor = "#111111";
    private String defaultBgColor = "#00000000";
    private int defaultTextSize = 60;

    private int mStartX = 70;
    private int mStartY = 100;

    public WatermarkTimeInfo() {
        createVertexBuffer();
    }

    /**
     * 时间水印构造方法
     *
     * @param startX 时间水印相对于屏幕上的X轴起始位置
     * @param startY 时间水印相对于屏幕上的Y轴起始位置
     */
    public WatermarkTimeInfo(int startX, int startY) {
        this.mStartX = startX;
        this.mStartY = startY;
        createVertexBuffer();
    }

    public WatermarkTimeInfo( String bgColor, String textColor, int textSize, int startX, int startY) {
        this.defaultBgColor = bgColor;
        this.defaultTextColor = textColor;
        this.defaultTextSize = textSize;
        this.mStartX = startX;
        this.mStartY = startY;
        createVertexBuffer();
    }

    @Override
    public void createVertexBuffer() {
        for (int i = 0; i < bitmaps.length; i++) {
            if (i <= 9) {
                bitmaps[i] = createTextImage(String.valueOf(i));
            } else if (i == 10) {
                bitmaps[i] = createTextImage(":");
            } else if (i == 11) {
                bitmaps[i] = createTextImage("-");
            } else if (i == 12) {
                bitmaps[i] = createTextImage(" ");
            }
        }

        Bitmap bitmap = bitmaps[0];

        Log.d(TAG, "createVertexBuffer: " + bitmap.getWidth());
        int currentStartX = mStartX;
        for (int i = 0; i < points.length; i++) {
            if (i == 13 || i == 16) {  //:符号宽度特殊点
//                points[i] = calculateWaterXY(currentStartX, mStartY, bitmaps[10].getWidth(), bitmaps[10].getHeight());
                initWaterVertexData(currentStartX, mStartY, bitmaps[10].getWidth(), bitmaps[10].getHeight(),waterVertexData);
                currentStartX += bitmaps[10].getWidth();
            } else if (i == 4 || i == 7) { // "-"
//                points[i] = calculateWaterXY(currentStartX, mStartY, bitmaps[11].getWidth(), bitmaps[11].getHeight());
                initWaterVertexData(currentStartX, mStartY, bitmaps[11].getWidth(), bitmaps[11].getHeight(),waterVertexData);
                currentStartX += bitmaps[11].getWidth();
            } else if (i == 10) {  // " "
//                points[i] = calculateWaterXY(currentStartX, mStartY, bitmaps[12].getWidth(), bitmaps[12].getHeight());
                initWaterVertexData(currentStartX, mStartY, bitmaps[12].getWidth(), bitmaps[12].getHeight(),waterVertexData);
                currentStartX += bitmaps[12].getWidth();
            } else {  //正常数字
//                points[i] = calculateWaterXY(currentStartX, mStartY, bitmap.getWidth(), bitmap.getHeight());
                initWaterVertexData(currentStartX, mStartY, bitmap.getWidth(), bitmap.getHeight(),waterVertexData);
                currentStartX += bitmap.getWidth();
            }

//            initData(waterVertexData, points[i]);
            vertexBuffers[i] = createFloatBuffer(waterVertexData);
        }

//        for (int i = 0; i < vertexBuffers.length; i++) {
//            initData(waterVertexData, points[i]);
//            vertexBuffers[i] = createFloatBuffer(waterVertexData);
//        }
    }

    private Bitmap createTextImage(String text) {
        return ShaderUtil.createTextImage(text, defaultTextSize, defaultTextColor, defaultBgColor, 0);
    }

    public int getNumFromDateStr(int i) {
        String dateStr = format.format(new Date());
        char ch = dateStr.charAt(i);
        if (Character.isDigit(ch)) {
            int dateNum = Integer.parseInt(String.valueOf(ch));
            return dateNum;
        } else if (ch == ':') {
            return 10;
        } else if (ch == '-') {
            return 11;
        }
        return 12;
    }

    public void release() {
        BYDLog.d(TAG, "release: ");
        for (Bitmap bitmap : bitmaps) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
        }
    }


}
