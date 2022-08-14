package com.byd.videoSdk.common.util;

import android.util.Log;

/**
 * Log tools.
 *
 */
public class BYDLog {

    private static final String TAG = "SENTRYMODE_SDK";
    public static final int LOG_LEVEL_V = 4;
    public static final int LOG_LEVEL_D = 3;
    public static final int LOG_LEVEL_I = 2;
    public static final int LOG_LEVEL_W = 1;
    public static final int LOG_LEVEL_E = 0;
    public static int LOG_LEVEL = 3;

    /**
     * Log v
     *
     * @param tag
     * @param msg
     */
    public static void v(String tag, String msg) {
        if (LOG_LEVEL >= LOG_LEVEL_V) {
            Log.d(TAG, tag + " : " + msg);
        }
    }

    /**
     * Log d
     *
     * @param tag
     * @param msg
     */
    public static void d(String tag, String msg) {
        if (LOG_LEVEL >= LOG_LEVEL_D) {
            Log.d(TAG, tag + " : " + msg);
        }
    }

    /**
     * Log i
     *
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg) {
        if (LOG_LEVEL >= LOG_LEVEL_I) {
            Log.i(TAG, tag + " : " + msg);
        }
    }

    /**
     * Log w
     *
     * @param tag
     * @param msg
     */
    public static void w(String tag, String msg) {
        if (LOG_LEVEL >= LOG_LEVEL_W) {
            Log.w(TAG, tag + " : " + msg);
        }
    }

    /**
     * Log w for excetion.
     *
     * @param tag
     * @param msg
     * @param e
     */
    public static void w(String tag, String msg, Exception e) {
        if (LOG_LEVEL >= LOG_LEVEL_W) {
            Log.w(TAG, tag + " : " + msg, e);
        }
    }

    /**
     * Log e
     *
     * @param tag
     * @param msg
     */
    public static void e(String tag, String msg) {
        Log.d(TAG, tag + " : " + msg);
    }

    /**
     * Logd
     *
     * @param tag2
     * @param string
     * @param e
     */
    public static void d(String tag2, String string, Exception e) {
        // TODO Auto-generated method stub
        Log.d(tag2, string, e);
    }

    /**
     * Logd
     *
     * @param tag2
     * @param string
     * @param e
     */
    public static void e(String tag2, String string, Exception e) {
        // TODO Auto-generated method stub
        Log.d(tag2, string, e);
    }

    public  static void s(String tag, String msg) {
        if (tag == null || tag.length() == 0
                || msg == null || msg.length() == 0)
            return;

        int segmentSize = 3 * 1024;
        long length = msg.length();
        if (length <= segmentSize ) {// 长度小于等于限制直接打印
            Log.d(TAG, tag + " : " + msg);
        }else {
            while (msg.length() > segmentSize ) {// 循环分段打印日志
                String logContent = msg.substring(0, segmentSize );
                msg = msg.replace(logContent, "");
                Log.d(tag,logContent);
            }
            Log.d(TAG, tag + " : " + msg);// 打印剩余日志
        }
    }
}
