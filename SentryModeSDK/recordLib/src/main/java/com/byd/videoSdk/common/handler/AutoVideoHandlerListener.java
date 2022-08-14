package com.byd.videoSdk.common.handler;

public interface AutoVideoHandlerListener {
    public void onNotify(int msg, int ext1, int ext2, Object obj);

    public boolean isCallback(int msg, int type);
}


