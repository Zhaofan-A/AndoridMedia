package com.byd.videoSdk.common.handler;

import com.byd.videoSdk.common.util.BYDLog;

public class SentryModeHandler {
    private static final String TAG = "SentryModeHandler";

    public static boolean isCallback(int msg, int type) {
        if (type == SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_ALL) {
            BYDLog.d(TAG, "MSG_TYPE_ALL callback");
            return true;
        }
        int result = (msg >> SentryModeHandlerMsg.MSG_SHIFT_BIT) & type;
        BYDLog.d(TAG, "isCallback: result " + result);
        switch (result) {
            case SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_TFCARD:
                return true;
            case SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_UI:
                return true;
            case SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_MEDIA:
                return true;
            case SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD:
                return true;
            case SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_MODEL:
                return true;
            case SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_TF_OBSERVE:
                return true;
            case SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECOVER:
                return true;
            default:
                break;
        }
        return false;
    }
}
