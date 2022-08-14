package com.byd.videoSdk.interfaces;

public interface IWatermarkRender {
    void reset();
    void onInitPosition(int avPosition, int afPosition);
    void onDrawFrame();
    void onRelease();
}
