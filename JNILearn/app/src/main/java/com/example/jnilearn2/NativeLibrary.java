package com.example.jnilearn2;

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
    public native int[] detection(byte[] buf,int w, int h);
    public native boolean detection2(byte[] buf , int w, int h);
    public native void init(int w,int h, float oversize_scale);
    public native void destroy();
}
