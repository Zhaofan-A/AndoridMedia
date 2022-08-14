package com.byd.videoSdk.common.handler;

import android.os.Handler;

/**
 *
 */
public interface AutoVideoHandlerInterface {

    void registerHandlerListener(AutoVideoHandlerListener listener, int type);

    void unRegisterHandlerListener(AutoVideoHandlerListener listener);

    class AutoVideoHandlerMessage {
        public static void sendHandlerMessage(int mode, Handler handler, int what, int arg1, int arg2, Object obj, int delay) {
            if (handler == null) {
                return;
            }

            if (mode == AutoVideoHandlerMsg.MSG_MODE_REMOVE_SEND) {
                handler.removeMessages(what);
            } else if (mode == AutoVideoHandlerMsg.MSG_MODE_CONTAIN_SEND && handler.hasMessages(what)) {
                return;
            }

            handler.sendMessageDelayed(handler.obtainMessage(what, arg1, arg2, obj), delay);
        }

        public static void sendHandlerMessage(int mode, Handler handler, int what) {
            sendHandlerMessage(mode, handler, what, 0, 0, null, 0);
        }

        public static void sendHandlerMessageDelay(int mode, Handler handler, int what, int delay) {
            sendHandlerMessage(mode, handler, what, 0, 0, null, delay);
        }

        public static void sendHandlerMessage(int mode, Handler handler, int what, Object obj) {
            sendHandlerMessage(mode, handler, what, 0, 0, obj, 0);
        }

        public static void sendHandlerMessage(int mode, Handler handler, int what, Object obj, int delay) {
            sendHandlerMessage(mode, handler, what, 0, 0, obj, delay);
        }

        public static void sendHandlerMessage(int mode, Handler handler, int what, int arg1, int arg2) {
            sendHandlerMessage(mode, handler, what, arg1, arg2, null, 0);
        }

        public static void removeMessages(Handler handler, int what) {
            handler.removeMessages(what);
        }

    }

}
