package com.byd.videoSdk.recorder.listener;

public interface VideoStateListener {
    //开始录制
    int VIDEO_START  = 0;
    //停止录制
    int VIDEO_STOP = 1;
    //视频成功落盘
    int VIDEO_SYNC_END  = 2;

    /**
     * 录制状态
     *
     * @param isLoopVideo  是否是循环视频
     * @param videoName  视频全路径名
     * @param status START: {@link VideoStateListener#VIDEO_START} <br>
     *               STOP: {@link VideoStateListener#VIDEO_STOP} <br>
     *               SYNC_END: {@link VideoStateListener#VIDEO_SYNC_END} <br>
     */
    void onVideoStatusChange(boolean isLoopVideo, String videoName, int status);
}
