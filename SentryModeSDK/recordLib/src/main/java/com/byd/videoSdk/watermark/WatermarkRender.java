package com.byd.videoSdk.watermark;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.byd.videoSdk.common.util.BYDLog;
import com.byd.videoSdk.interfaces.IWatermarkRender;
import com.byd.videoSdk.render.CameraRender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class WatermarkRender implements IWatermarkRender {

    private String TAG = "WatermarkRender";

    static float waterVertexData[] = {
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f
    };

    //纹理坐标  对应顶点坐标  与之映射
    static float textureData[] = {   // in counterclockwise order:
            0f, 1f, 0.0f, // bottom left
            1f, 1f, 0.0f, // bottom right
            0f, 0f, 0.0f, // top left
            1f, 0f, 0.0f,  // top right
    };

    private int avPosition = -1;
    private int afPosition = -1;


    //纹理
    private FloatBuffer textureBuffer;

    private List<WatermarkTimeInfo> timeInfos = new ArrayList<>();
    private List<WatermarkBitmapInfo> bitmapInfos = new ArrayList<>();
    private List<WatermarkTextInfo> textInfos = new ArrayList<>();

    private String renderType;
    private Context context;

    private int lastNum = -1;
    private int lastPostion = -1;

    private boolean initTime = false;
    private boolean initBitmap = false;
    private boolean initText = false;

    private boolean positionInit = false;

    protected WatermarkRender(Context context, String type) {
        BYDLog.d(TAG, "WatermarkRender: ");
        this.context = context;
        textureBuffer = createFloatBuffer(textureData);

        renderType = type;
        TAG = renderType + "_" + TAG;
    }


    private FloatBuffer createFloatBuffer(float[] datas) {
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(datas.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(datas);
        floatBuffer.position(0);
        return floatBuffer;
    }

    /**
     * 创建时间水印vbo
     */
    private void initTimeVboAndTextureId() {
        for (int i = 0; i < timeInfos.size(); i++) {
            WatermarkTimeInfo watermarkTimeInfo = timeInfos.get(i);
            if (!watermarkTimeInfo.isInitData) {
                for (int t = 0; t < watermarkTimeInfo.vertexBuffers.length; t++) {
                    watermarkTimeInfo.vboIds[t] = createVBO(watermarkTimeInfo.vertexBuffers[t]);
                }

                for (int t = 0; t < watermarkTimeInfo.bitmaps.length; t++) {
                    watermarkTimeInfo.waterTextureIds[t] = createWaterTextureId(watermarkTimeInfo.bitmaps[t]);
                }
                watermarkTimeInfo.isInitData = true;
            }
        }
    }

    /**
     * 创建文字水印vbo
     */
    private void initTextVboAndTextureId() {
        for (int i = 0; i < textInfos.size(); i++) {
            WatermarkTextInfo textInfo = textInfos.get(i);
            if (!textInfo.isInitData) {
                textInfo.vboId = createVBO(textInfo.vertexBuffers);
                textInfo.waterTextureId = createWaterTextureId(textInfo.bitmap);
                textInfo.isInitData = true;
            }
        }
    }

    /**
     * 创建图片水印vbo
     */
    private void initBitmapVboAndTextureId() {
        for (int i = 0; i < bitmapInfos.size(); i++) {
            WatermarkBitmapInfo bitmapInfo = bitmapInfos.get(i);
            if (!bitmapInfo.isInitData) {
                bitmapInfo.vboId = createVBO(bitmapInfo.vertexBuffers);
                bitmapInfo.waterTextureId = createWaterTextureId(bitmapInfo.bitmap);
                bitmapInfo.isInitData = true;
            }
        }
    }

    /**
     * 创建vbo
     */
    private int createVBO(FloatBuffer floatBuffer) {
        //1. 创建VBO
        int[] vbos = new int[1];
        GLES20.glGenBuffers(vbos.length, vbos, 0);
        int vboId = vbos[0];
        //2. 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        //3. 分配VBO需要的缓存大小
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, waterVertexData.length * 4 + textureData.length * 4, null, GLES20.GL_STATIC_DRAW);
        //4. 为VBO设置顶点数据的值
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, waterVertexData.length * 4, floatBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, waterVertexData.length * 4, textureData.length * 4, textureBuffer);
        //5. 解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        return vboId;
    }


    private int createWaterTextureId(Bitmap bitmap) {
        int[] textureIds = new int[1];
        //创建纹理
        GLES20.glGenTextures(1, textureIds, 0);

        int waterTextureId = textureIds[0];
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, waterTextureId);
        //环绕（超出纹理坐标范围）  （s==x t==y GL_REPEAT 重复）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //过滤（纹理像素映射到坐标点）  （缩小、放大：GL_LINEAR线性）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        ByteBuffer bitmapBuffer = ByteBuffer.allocate(bitmap.getHeight() * bitmap.getWidth() * 4);//RGBA
        bitmap.copyPixelsToBuffer(bitmapBuffer);
        //将bitmapBuffer位置移动到初始位置
        bitmapBuffer.flip();

        //设置内存大小绑定内存地址
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(),
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bitmapBuffer);

        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        return waterTextureId;
    }

    public void reset() {
        BYDLog.d(TAG, "reset: ");
        positionInit = false;
        initTime = false;
        initBitmap = false;
        initText = false;
        for (int i = 0; i < timeInfos.size(); i++) {
            WatermarkTimeInfo watermarkTimeInfo = timeInfos.get(i);
            watermarkTimeInfo.isInitData = false;
        }
        for (int i = 0; i < textInfos.size(); i++) {
            WatermarkTextInfo textInfo = textInfos.get(i);
            textInfo.isInitData = false;
        }
        for (int i = 0; i < bitmapInfos.size(); i++) {
            WatermarkBitmapInfo bitmapInfo = bitmapInfos.get(i);
            bitmapInfo.isInitData = false;
        }
    }

    @Override
    public void onInitPosition(int avPosition, int afPosition) {
        if (!positionInit) {
            BYDLog.d(TAG, "onInitPosition: " + timeInfos.size());
            this.avPosition = avPosition;
            this.afPosition = afPosition;
            positionInit = true;
        }
    }

    @Override
    public void onDrawFrame() {
        if (!initTime) {
            initTimeVboAndTextureId();
            initTime = true;
        }
        if (!initBitmap) {
            initBitmapVboAndTextureId();
            initBitmap = true;
        }
        if (!initText) {
            initTextVboAndTextureId();
            initText = true;
        }

        //画时间水印
        for (int t = 0; t < timeInfos.size(); t++) {
            WatermarkTimeInfo watermarkTimeInfo = timeInfos.get(t);
            for (int i = 0; i < watermarkTimeInfo.vboIds.length; i++) {
                drawTimeWater(watermarkTimeInfo, watermarkTimeInfo.getNumFromDateStr(i), i);
            }
        }
        //画bitmap水印
        for (int i = 0; i < bitmapInfos.size(); i++) {
            WatermarkBitmapInfo watermarkBitmapInfo = bitmapInfos.get(i);
            if (watermarkBitmapInfo.vboId != -1) {
                drawWater(watermarkBitmapInfo.vboId, watermarkBitmapInfo.waterTextureId);
            }
        }

        //画Text水印
        for (int i = 0; i < textInfos.size(); i++) {
            WatermarkTextInfo watermarkTextInfo = textInfos.get(i);
            if (watermarkTextInfo.vboId != -1) {
                drawWater(watermarkTextInfo.vboId, watermarkTextInfo.waterTextureId);
            }
        }

    }

    private void drawTimeWater(WatermarkTimeInfo watermarkTimeInfo, int num, int position) {
       // Log.d(TAG, "drawTimeWater:  num = " + num + "    position = " + position);
        if (lastNum != num || lastPostion != position) {
            drawWater(watermarkTimeInfo.vboIds[position], watermarkTimeInfo.waterTextureIds[num]);
        }
        lastNum = num;
        lastPostion = position;
    }

    private void drawWater(int vboId, int textureId) {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glEnableVertexAttribArray(afPosition);

        GLES20.glVertexAttribPointer(avPosition, CameraRender.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, CameraRender.vertexStride,
                0);//四个坐标之后的是水印的坐标
        GLES20.glVertexAttribPointer(afPosition, CameraRender.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, CameraRender.vertexStride,
                textureData.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(avPosition);
        GLES20.glDisableVertexAttribArray(afPosition);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void addWatermarkTime(WatermarkTimeInfo watermarkTimeInfo) {
        BYDLog.d(TAG, "addWatermarkTime:");
        timeInfos.add(watermarkTimeInfo);
        initTime = false;
    }

    public void addWatermarkBitmap(WatermarkBitmapInfo watermarkBitmapInfo) {
        BYDLog.d(TAG, "addWatermarkBitmap:");
        bitmapInfos.add(watermarkBitmapInfo);
        initBitmap = false;
    }

    public void addWatermarkText(WatermarkTextInfo watermarkTextInfo) {
        BYDLog.d(TAG, "addWatermarkText:");
        textInfos.add(watermarkTextInfo);
        initText = false;
    }

    public List<WatermarkTimeInfo> getWatermarkTimeList() {
        return timeInfos;
    }

    public List<WatermarkBitmapInfo> getWatermarkBitmpList() {
        return bitmapInfos;
    }

    public List<WatermarkTextInfo> getWatermarkTextList() {
        return textInfos;
    }

    @Override
    public void onRelease() {
        BYDLog.d(TAG, "onRelease:  renderType= " + renderType);
        for (WatermarkBitmapInfo watermarkBitmapInfo : bitmapInfos) {
            watermarkBitmapInfo.release();
        }
        for (WatermarkTextInfo watermarkTextInfo : textInfos) {
            watermarkTextInfo.release();
        }
        for (WatermarkTimeInfo watermarkTimeInfo : timeInfos) {
            watermarkTimeInfo.release();
        }
        bitmapInfos.clear();
        textInfos.clear();
        timeInfos.clear();
        WatermarkFactor.getInstance(context).removeWatermarkRender(renderType);
        positionInit = false;
    }

}
