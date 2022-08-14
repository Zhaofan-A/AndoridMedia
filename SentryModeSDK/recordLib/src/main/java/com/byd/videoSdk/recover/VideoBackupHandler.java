package com.byd.videoSdk.recover;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.byd.videoSdk.common.util.BYDLog;

/**
 * For process backup msg.
 *
 * @author bai.yu1
 */
public class VideoBackupHandler {
    private static VideoBackupHandler mInstance = null;
    private Handler mHandler = null;
    private HandlerThread mThread = null;

    private VideoBackupHandler() {
        //创建一个线程,线程名字：handler-thread
        mThread = new HandlerThread("BackUp handler thread");
        //开启一个线程
        mThread.start();
        //在这个线程中创建一个handler对象
        mHandler = new Handler(mThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //这个方法是运行在 handler-thread 线程中的 ，可以执行耗时操作
                BYDLog.d("handler ", "msg: " + msg.what + "  thread: " + Thread.currentThread().getName());
            }
        };
    }

    /**
     * Get the instance.
     *
     * @return
     */
    public synchronized static VideoBackupHandler getInstance() {
        if (mInstance == null) {
            mInstance = new VideoBackupHandler();
        }
        return mInstance;
    }

    /**
     * Post a runnbale.
     *
     * @param r
     */
    public void postRunnable(Runnable r) {
        if (mHandler != null) {
            mHandler.post(r);
        }
    }

    /**
     * Remove the runnable.
     *
     * @param r
     */
    public void removeRunnable(Runnable r) {
        if (mHandler != null) {
            mHandler.removeCallbacks(r);
        }
    }

    /**
     * Has runnable not process.
     *
     * @param r
     * @return
     */
    public boolean hasRunnable(Runnable r) {
        if (mHandler != null) {
            return mHandler.hasCallbacks(r);
        }
        return false;
    }
}
