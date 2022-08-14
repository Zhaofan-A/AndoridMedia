package com.byd.videoSdk.watermark;

public class NativeLibrary {
    static {
        System.loadLibrary("native-lib");
    }
    private volatile static NativeLibrary mInstance;
    /**
     * 获取NativeLibrary实例
     */
    public static NativeLibrary getInstance(){
        if (mInstance == null) {
            synchronized (NativeLibrary.class) {
                if (mInstance == null) {
                    mInstance = new NativeLibrary();
                }
            }
        }
        return mInstance;
    }

    public native void initWatermark();
    public native String stringFromJNI();
    public native void initWaterVertexData(int x, int y, int w, int h, int width, int height , float[] outVertexData);
}
