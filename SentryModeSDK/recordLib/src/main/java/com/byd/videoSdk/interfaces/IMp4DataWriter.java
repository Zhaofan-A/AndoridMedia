package com.byd.videoSdk.interfaces;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public interface IMp4DataWriter {

    int SYNC_COST_TIME = 3 * 1000;

    boolean setVideoMediaFormat(MediaFormat f);
    void writeSampeData(ByteBuffer buffer, MediaCodec.BufferInfo info);
    void close();
}
