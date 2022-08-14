package com.byd.videoSdk.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.byd.videoSdk.R;
import com.byd.videoSdk.common.util.BYDLog;
import com.byd.videoSdk.common.util.DisplayUtil;
import com.byd.videoSdk.egl.EglController;
import com.byd.videoSdk.egl.EglSurfaceView;
import com.byd.videoSdk.common.util.ShaderUtil;
import com.byd.videoSdk.interfaces.IWatermarkRender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class CameraFboRender implements EglSurfaceView.Render, SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "CameraFboRender";
    //顶点坐标
    private static float vertexData[] = {   // in counterclockwise order:
            -1f, -1f, 0.0f, // bottom left
            1f, -1f, 0.0f, // bottom right
            -1f, 1f, 0.0f, // top left
            1f, 1f, 0.0f,  // top right
    };

    //纹理坐标  对应顶点坐标  与之映射
    private static float textureData[] = {   // in counterclockwise order:
            0f, 1f, 0.0f, // bottom left
            1f, 1f, 0.0f, // bottom right
            0f, 0f, 0.0f, // top left
            1f, 0f, 0.0f,  // top right
    };
    //每一次取点的时候取几个点
    private static final int COORDS_PER_VERTEX = 3;

    private final int vertexCount = vertexData.length / COORDS_PER_VERTEX;
    //每一次取的总的点 大小
    private static final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    //位置
    private FloatBuffer vertexBuffer;
    //纹理
    private FloatBuffer textureBuffer;
    private int program;
    private int avPosition;
    //纹理位置
    private int afPosition;
    //变换矩阵 Id
    private int uMatrix;
    //fbo Id
    private int fboId;
    //fbo纹理id
    private int fboTextureId;
    //camera纹理id
    private int cameraRenderTextureId;
    //vbo id
    private int vboId;

    //变换矩阵
    private float[] matrix = new float[16];

    private int screenW, screenH;

    private Context context;
    private SurfaceTexture surfaceTexture;

    private CameraRender cameraRender;

    private OnSurfaceTextureListener onSurfaceListener;

    boolean mFrameAvailableEnable = false;
    boolean isPreview = false;

    public CameraFboRender(Context context,boolean isPreview) {
        this.context = context;
        screenW = DisplayUtil.getScreenW(context);
        screenH = DisplayUtil.getScreenH(context);

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureBuffer = ByteBuffer.allocateDirect(textureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureData);
        textureBuffer.position(0);

        this.isPreview = isPreview;

        if(isPreview){
            cameraRender = new CameraRender(context);
        }
    }


    @Override
    public void onSurfaceCreated() {

        program = ShaderUtil.createProgram(ShaderUtil.readRawTxt(context, R.raw.vertex_shader),
                ShaderUtil.readRawTxt(context, R.raw.fragment_shader));

        if (program > 0) {
            //获取顶点坐标字段
            avPosition = GLES20.glGetAttribLocation(program, "av_Position");
            //获取纹理坐标字段
            afPosition = GLES20.glGetAttribLocation(program, "af_Position");
            uMatrix = GLES20.glGetUniformLocation(program, "u_Matrix");

            //创建vbo
            createVBO();

            //创建fbo
            createFBO(screenW, screenH);

            //创建相机预览扩展纹理
            createCameraRenderTexture();
        }
        if(isPreview){
            cameraRender.onSurfaceCreated();
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        if(cameraRender!=null) {
            cameraRender.onSurfaceChanged(width, height);
        }
    }

    @Override
    public void onDrawFrame() {
        //调用触发onFrameAvailable
        surfaceTexture.updateTexImage();
        //清空颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //设置背景颜色
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        //使用程序
        GLES20.glUseProgram(program);

        //绑定fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        //摄像头预览扩展纹理赋值
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraRenderTextureId);

        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glEnableVertexAttribArray(afPosition);

        //给变换矩阵赋值
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, matrix, 0);

        //使用VBO设置纹理和顶点值
        useVboSetVertext();

        //绘制 GLES20.GL_TRIANGLE_STRIP:复用坐标
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(avPosition);
        GLES20.glDisableVertexAttribArray(afPosition);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        if(cameraRender!=null) {
            cameraRender.onDraw(fboTextureId);
        }
    }

    @Override
    public void onDestory() {
        BYDLog.d(TAG, "onDestory: ");
        if(cameraRender!=null) {
            cameraRender.onDestory();
        }
    }

    @Override
    public void onRelease() {
        BYDLog.d(TAG, "onRelease: ");
        if(cameraRender!=null) {
            cameraRender.onRelease();
        }
    }

    /**
     * 初始化矩阵
     */
    public void resetMatirx() {
        //初始化
        Matrix.setIdentityM(matrix, 0);
    }

    /**
     * 旋转
     *
     * @param angle
     * @param x
     * @param y
     * @param z
     */
    public void setAngle(float angle, float x, float y, float z) {
        //旋转
        Matrix.rotateM(matrix, 0, angle, x, y, z);
    }

    public void setFrameAvailableEnable(boolean enable){
        mFrameAvailableEnable = enable;
    }


    /**
     * 创建vbo
     */
    private void createVBO() {
        //1. 创建VBO
        int[] vbos = new int[1];
        GLES20.glGenBuffers(vbos.length, vbos, 0);
        vboId = vbos[0];
        //2. 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        //3. 分配VBO需要的缓存大小
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + textureData.length * 4, null, GLES20.GL_STATIC_DRAW);
        //4. 为VBO设置顶点数据的值
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, textureData.length * 4, textureBuffer);
        //5. 解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    /**
     * 使用vbo设置顶点位置
     */
    private void useVboSetVertext() {
        //1. 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        //2. 设置顶点数据
        GLES20.glVertexAttribPointer(avPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, 0);
        GLES20.glVertexAttribPointer(afPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexData.length * 4);
        //3. 解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }


    /**
     * 创建fbo
     *
     * @param w
     * @param h
     */
    private void createFBO(int w, int h) {
        BYDLog.d(TAG, "createFBO: w =" +w +"  h= "+h);
        
        //1. 创建FBO
        int[] fbos = new int[1];
        GLES20.glGenFramebuffers(1, fbos, 0);
        fboId = fbos[0];
        //2. 绑定FBO  告诉opengl这是一个framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        //3. 创建FBO纹理
        int[] textureIds = new int[1];
        //创建纹理
        GLES20.glGenTextures(1, textureIds, 0);
        fboTextureId = textureIds[0];
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId);
        //环绕（超出纹理坐标范围）  （s==x t==y GL_REPEAT 重复）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //过滤（纹理像素映射到坐标点）  （缩小、放大：GL_LINEAR线性）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        //4. 把纹理绑定到FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, fboTextureId, 0);

        //5. 设置FBO分配内存大小
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, w, h,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        //6. 检测是否绑定从成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
                != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            BYDLog.e(TAG, "glFramebufferTexture2D error");
        }
        //7. 解绑纹理和FBO
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    /**
     * 创建摄像头预览扩展纹理
     */
    private void createCameraRenderTexture() {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        cameraRenderTextureId = textureIds[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraRenderTextureId);
        //环绕（超出纹理坐标范围）  （s==x t==y GL_REPEAT 重复）
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //过滤（纹理像素映射到坐标点）  （缩小、放大：GL_LINEAR线性）
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        surfaceTexture = new SurfaceTexture(cameraRenderTextureId);
        surfaceTexture.setOnFrameAvailableListener(this);

        if (onSurfaceListener != null) {
            onSurfaceListener.onSurfaceTextureCreate(surfaceTexture, fboTextureId);
        }

        // 解绑扩展纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if(mFrameAvailableEnable){
            BYDLog.d(TAG, "onFrameAvailable: ");
            onDrawFrame();
            EglController.getInstance().sendMessagToEGLHandler(EglController.WHAT_SWAP);
        }
    }

    public boolean takingPhoto() {
        if(cameraRender!=null) {
           return cameraRender.takingPhoto();
        }
        return false;
    }

    public void setWatermarkRender(IWatermarkRender watermarkRender){
        if(cameraRender!=null) {
            cameraRender.setWatermarkRender(watermarkRender);
        }
    }

    public void setOnSurfaceListener(OnSurfaceTextureListener onSurfaceListener) {
        this.onSurfaceListener = onSurfaceListener;
    }

    public interface OnSurfaceTextureListener {
        void onSurfaceTextureCreate(SurfaceTexture surfaceTexture, int fboTextureId);
    }

}
