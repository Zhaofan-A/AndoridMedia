package com.byd.videoSdk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.byd.videoSdk.common.handler.AutoVideoHandler;
import com.byd.videoSdk.common.handler.AutoVideoHandlerListener;
import com.byd.videoSdk.common.handler.SentryModeHandler;
import com.byd.videoSdk.common.handler.SentryModeHandlerMsg;
import com.byd.videoSdk.common.util.BYDLog;
import com.byd.videoSdk.recorder.RecorderController;
import com.byd.videoSdk.recorder.utils.FileSwapHelper;
import com.byd.videoSdk.recover.Mp4ResumeController;

public class SdkService extends Service implements AutoVideoHandlerListener {
    private static final String TAG = "SdkService";

    @Override
    public void onCreate() {
        super.onCreate();
        AutoVideoHandler.getInstance().registerHandlerListener(this, SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD|
                                                                                     SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECOVER);
        Mp4ResumeController.getInstance().startCheck();
        BYDLog.d(TAG, "onCreate: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BYDLog.d(TAG, "onDestroy: ");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onNotify(int msg, int ext1, int ext2, Object obj) {
        BYDLog.d(TAG, "onNotify:  msg = "+SentryModeHandlerMsg.getMessageName(msg));
        switch (msg){
            case SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD_VIDEO_END:
                String videoName = (String) obj;
                boolean isLoop = videoName.contains(FileSwapHelper.NORMAL);
                if(!isLoop){
                    RecorderController.getInstance().stopRecord();
                }
                break;
            case SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD_VIDEO_SYNC_END:
                break;
            case SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD_VIDEO_START:
                break;
            case SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECOVER_VIDEO_END:
                break;
            default:break;
        }
    }

    @Override
    public boolean isCallback(int msg, int type) {
        return SentryModeHandler.isCallback(msg, type);
    }
}
