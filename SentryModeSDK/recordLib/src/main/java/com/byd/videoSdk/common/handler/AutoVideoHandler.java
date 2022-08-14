package com.byd.videoSdk.common.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import com.byd.videoSdk.recorder.RecorderController;
import com.byd.videoSdk.common.util.BYDLog;

import java.util.HashMap;
import java.util.Iterator;

public class AutoVideoHandler implements Handler.Callback,AutoVideoHandlerInterface {
    private static final String TAG = "AutoVideoHandler";
    HashMap<AutoVideoHandlerListener, Integer> mAutoVideoHandlerListener = new HashMap<AutoVideoHandlerListener, Integer>();
    private Handler mMainLooperHandler;

    private AutoVideoHandler() {
        Looper looper = RecorderController.getInstance().getContext().getMainLooper();
        mMainLooperHandler = new Handler(looper, this);
    }

    private static final AutoVideoHandler single = new AutoVideoHandler();

    public static AutoVideoHandler getInstance() {
        return single;
    }

    public void sendMessage(int what) {
        sendMessage(AutoVideoHandlerMsg.MSG_MODE_REMOVE_SEND, what, 0, 0, null, 0);
    }

    public void sendMessage(int what, int delay) {
        sendMessage(AutoVideoHandlerMsg.MSG_MODE_REMOVE_SEND, what, 0, 0, null, delay);
    }

    public void sendMessage(int what, Object obj) {
        sendMessage(AutoVideoHandlerMsg.MSG_MODE_REMOVE_SEND, what, 0, 0, obj, 0);
    }

    public void sendMessage(int what, int arg1, int arg2) {
        sendMessage(AutoVideoHandlerMsg.MSG_MODE_REMOVE_SEND, what, arg1, arg2, null, 0);
    }

    public void sendMessage(int what, int arg1, Object obj) {
        sendMessage(AutoVideoHandlerMsg.MSG_MODE_REMOVE_SEND, what, arg1, 0, obj, 0);
    }

    public void sendMessage(int what, int arg1, int arg2, Object obj, int delay) {
          AutoVideoHandlerInterface.AutoVideoHandlerMessage.sendHandlerMessage(AutoVideoHandlerMsg.MSG_MODE_REMOVE_SEND, mMainLooperHandler, what, arg1, arg2, obj, delay);
    }

    public void sendMessage(int mode, int what, int arg1, int arg2, Object obj, int delay) {
        AutoVideoHandlerInterface.AutoVideoHandlerMessage.sendHandlerMessage(AutoVideoHandlerMsg.MSG_MODE_REMOVE_SEND, mMainLooperHandler, what, arg1, arg2, obj, delay);
    }

    public void sendDirectlyMessage(int what) {
        sendDirectlyMessage(AutoVideoHandlerMsg.MSG_MODE_DIRECTLY_SEND, what, 0, 0, null, 0);
    }

    public void sendDirectlyMessage(int what, int delay) {
        sendDirectlyMessage(AutoVideoHandlerMsg.MSG_MODE_DIRECTLY_SEND, what, 0, 0, null, delay);
    }

    public void sendDirectlyMessage(int what, Object obj) {
        sendDirectlyMessage(AutoVideoHandlerMsg.MSG_MODE_DIRECTLY_SEND, what, 0, 0, obj, 0);
    }

    public void sendDirectlyMessage(int what, int arg1, int arg2) {
        sendDirectlyMessage(AutoVideoHandlerMsg.MSG_MODE_DIRECTLY_SEND, what, arg1, arg2, null, 0);
    }

    public void sendDirectlyMessage(int what, int arg1, int arg2, Object obj, int delay) {
        AutoVideoHandlerInterface.AutoVideoHandlerMessage.sendHandlerMessage(AutoVideoHandlerMsg.MSG_MODE_DIRECTLY_SEND, mMainLooperHandler, what, arg1, arg2, obj, delay);
    }

    public void sendDirectlyMessage(int mode, int what, int arg1, int arg2, Object obj, int delay) {
        AutoVideoHandlerInterface.AutoVideoHandlerMessage.sendHandlerMessage(AutoVideoHandlerMsg.MSG_MODE_DIRECTLY_SEND, mMainLooperHandler, what, arg1, arg2, obj, delay);
    }

    /**
     * Add by baiyu for notify recorder files in 20190826.
     * 没有走Handler,就是普通的接口回调
     *
     * @param what
     * @param arg1
     * @param arg2
     * @param obj
     * @return
     */
    public boolean sendDirectlyMessageAsync(int what, int arg1, int arg2, Object obj) {
        try {
            Iterator iter = mAutoVideoHandlerListener.entrySet().iterator();
            while (iter.hasNext()) {
                HashMap.Entry entry = (HashMap.Entry) iter.next();
                AutoVideoHandlerListener listener = (AutoVideoHandlerListener) entry.getKey();
                Integer type = (Integer) entry.getValue();
                if (listener.isCallback(what, type)) {
                    listener.onNotify(what, arg1, arg2, obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean sendDirectlyMessageAsync(int what, Object obj) {
        try {
            Iterator iter = mAutoVideoHandlerListener.entrySet().iterator();
            while (iter.hasNext()) {
                HashMap.Entry entry = (HashMap.Entry) iter.next();
                AutoVideoHandlerListener listener = (AutoVideoHandlerListener) entry.getKey();
                Integer type = (Integer) entry.getValue();
                if (listener.isCallback(what, type)) {
                    listener.onNotify(what, 0, 0, obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean handleMessage(Message msg) {
        /** Add by zhou.shuanghua to fix T55961 2019.07.13 end */
        try {
            Iterator iter = mAutoVideoHandlerListener.entrySet().iterator();

            BYDLog.d(TAG, "handleMessage: "+msg.what);
            BYDLog.d(TAG, "handleMessage: "+iter.hasNext()+"        "+mAutoVideoHandlerListener.size());
            while (iter.hasNext()) {
                HashMap.Entry entry = (HashMap.Entry) iter.next();
                AutoVideoHandlerListener listener = (AutoVideoHandlerListener) entry.getKey();
                Integer type = (Integer) entry.getValue();

                BYDLog.d(TAG, "handleMessage: type = "+type);
                if (listener.isCallback(msg.what, type)) {
                    listener.onNotify(msg.what, msg.arg1, msg.arg2, msg.obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /** Add by zhou.shuanghua to fix T55961 2019.07.13 end */
        return false;
    }

    @Override
    public void registerHandlerListener(AutoVideoHandlerListener listener, int type) {
        BYDLog.d(TAG, "registerListener: " + listener + ",type:" + type);
        if (!mAutoVideoHandlerListener.containsKey(listener)) {
            mAutoVideoHandlerListener.put(listener, type);
        }
        BYDLog.d(TAG, "mMediaListener: " + mAutoVideoHandlerListener.size());
    }

    @Override
    public void unRegisterHandlerListener(AutoVideoHandlerListener listener) {
        BYDLog.e(TAG, "unRegisterListener: " + listener);
        if (mAutoVideoHandlerListener.containsKey(listener)) {
            mAutoVideoHandlerListener.remove(listener);
        }
    }

}
