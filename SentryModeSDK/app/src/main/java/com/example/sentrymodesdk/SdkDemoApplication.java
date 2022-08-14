package com.example.sentrymodesdk;

import android.app.Application;

import com.byd.videoSdk.recorder.RecorderController;

public class SdkDemoApplication extends Application {

    public static final String RECORD_DURATION = "record_duration";
    public static final String RECORD_WATERMARK = "record_watermark";
    public static final String RECORD_RECOVER = "record_recover";

    public static  int mDuration = 60;  //60秒，1分钟
    public static boolean mWatermark = true;
    public static boolean mRecorver = true;

    @Override
    public void onCreate() {
        super.onCreate();
        RecorderController  mRecorderController = RecorderController.getInstance();
        mRecorderController.init(this);
    }

}
