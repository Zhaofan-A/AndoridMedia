package com.byd.videoSdk.recorder.encodec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.SystemClock;

import com.byd.videoSdk.common.util.BYDLog;

import java.nio.ByteBuffer;

class VideoEncodecThread extends Thread {
    private static final String TAG = "VideoEncodecThread";

    private boolean isExit;

    private long pts;
    private MediaCodec mVideoEncodec;
    private MediaCodec.BufferInfo mVideoBuffInfo;
    private VideoCodeEncoder mCodeEncoder;

    private boolean encodeStart;

    public VideoEncodecThread(VideoCodeEncoder codeEncoder) {
        mCodeEncoder = codeEncoder;
        mVideoEncodec = codeEncoder.getMediaCodec();
        mVideoBuffInfo = codeEncoder.getMediaCodecBufferInfo();
        pts = 0;
    }

    @Override
    public void run() {
        super.run();
        encodeStart = false;
        isExit = false;
        mVideoEncodec.start();
        BYDLog.d(TAG, "run: start");
        while (!isExit) {
            int outputBufferIndex = mVideoEncodec.dequeueOutputBuffer(mVideoBuffInfo, 0);
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = mVideoEncodec.getOutputFormat();
                mCodeEncoder.callbackMediaFormat(newFormat);
                encodeStart = true;
                BYDLog.d(TAG, "run: out mediaFormat");
            } else {
                while (outputBufferIndex >= 0) {
                    if (!encodeStart) {
                        BYDLog.d(TAG, "run: encodeStart = false");
                        SystemClock.sleep(10);
                        continue;
                    }
                    ByteBuffer outputBuffer = mVideoEncodec.getOutputBuffers()[outputBufferIndex];
                    outputBuffer.position(mVideoBuffInfo.offset);
                    outputBuffer.limit(mVideoBuffInfo.offset + mVideoBuffInfo.size);

                    //设置时间戳
                    if (pts == 0) {
                        pts = mVideoBuffInfo.presentationTimeUs;
                    }
                    mVideoBuffInfo.presentationTimeUs = mVideoBuffInfo.presentationTimeUs - pts;
                    //写入数据
                    mCodeEncoder.callbackMuxerData(new VideoCodeEncoder.MediaCodecData(0, outputBuffer, mVideoBuffInfo));
                    mVideoEncodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mVideoEncodec.dequeueOutputBuffer(mVideoBuffInfo, 0);
                }
            }
        }
        mVideoEncodec.stop();
        mVideoEncodec.release();
        mVideoEncodec = null;
        BYDLog.d(TAG, "run: exited");
    }

    public void exit() {
        BYDLog.d(TAG, "exit");
        isExit = true;
        encodeStart = false;
    }
}
