package com.byd.videoSdk.recorder.buffer;


import com.byd.videoSdk.recorder.encodec.VideoCodeEncoder;
import com.byd.videoSdk.common.util.BYDLog;

import java.util.Vector;


/**
 * The data buffer.
 * Is from I frame to last p frame.
 * So the first must be I frame.
 *
 * @author bai.yu1
 */
public class DataBuffer {
    private static final String TAG = "DataBuffer";
    private long mTime;
    private long mBufSize = 0;
    //MediaFormat mediaFormat;
    private Vector<VideoCodeEncoder.MediaCodecData> mFrameData;

    /**
     * Init the data buffer.
     */
    public DataBuffer() {
        mFrameData = new Vector<VideoCodeEncoder.MediaCodecData>();
    }

    /**
     * The data buffer.
     *
     * @param initialcapacity
     * @param capacityIncrement
     */
    public DataBuffer(int initialcapacity, int capacityIncrement) {
        if (initialcapacity <= 0 || capacityIncrement <= 0) {
            BYDLog.w(TAG, "DataBuffer: the initialcapacity[" + initialcapacity + "]");
            BYDLog.w(TAG, "DataBuffer: the capacityIncrement[" + capacityIncrement + "]");
            BYDLog.w(TAG, "DataBuffer: will be inited default");
            mFrameData = new Vector<VideoCodeEncoder.MediaCodecData>();
        } else {
            mFrameData = new Vector<VideoCodeEncoder.MediaCodecData>(initialcapacity, capacityIncrement);
        }
    }

    /**
     * Get the time.
     *
     * @return
     */
    public synchronized long getTime() {
        return mTime;
    }

    /**
     * Set the the.
     *
     * @param time
     */
    public synchronized void setTime(long time) {
        mTime = time;
    }

    /**
     * Get the buf size.
     *
     * @return
     */
    public synchronized long getBufSize() {
        return mBufSize;
    }

    /**
     * Get the position.
     *
     * @param position
     * @return
     */
    public synchronized VideoCodeEncoder.MediaCodecData getMediaCodecData(int position) {
        if (mFrameData != null && mFrameData.size() > position) {
            return mFrameData.elementAt(position);
        }
        BYDLog.e(TAG, "getMediaCodecData: mFrameData is " + mFrameData);
        if (mFrameData != null) {
            BYDLog.e(TAG, "getMediaCodecData: mFrameData size is " + mFrameData.size());
        }
        return null;
    }

    /**
     * Add the data to last.
     *
     * @param data
     * @return
     */
    public synchronized boolean addMediaCodecData(VideoCodeEncoder.MediaCodecData data) {
        if (mFrameData != null) {
            mBufSize += data.bufferInfo.size;
            return mFrameData.add(data);
        }
        BYDLog.e(TAG, "addMediaCodecData: mFrameData is " + mFrameData);
        return false;
    }

    /**
     * Add the data to position.
     *
     * @param data
     * @param position
     * @return if position > size, return false;
     */
    public synchronized boolean addMediaCodecData(VideoCodeEncoder.MediaCodecData data, int position) {
        if (mFrameData != null && mFrameData.size() >= position) {
            mBufSize += data.bufferInfo.size;
            mFrameData.add(position, data);
            return true;
        }
        BYDLog.e(TAG, "addMediaCodecData: mFrameData is " + mFrameData);
        if (mFrameData != null) {
            BYDLog.e(TAG, "addMediaCodecData: mFrameData size is " + mFrameData.size());
        }
        BYDLog.e(TAG, "addMediaCodecData: position is " + position);
        return false;
    }

    /**
     * Get the data.
     *
     * @param position
     * @return
     */
    public synchronized VideoCodeEncoder.MediaCodecData get(int position) {
        // TODO Auto-generated method stub
        if (mFrameData != null && mFrameData.size() > position) {
            return mFrameData.get(position);
        }
        BYDLog.e(TAG, "get: mFrameData is " + mFrameData);
        if (mFrameData != null) {
            BYDLog.e(TAG, "get: mFrameData size is " + mFrameData.size());
        }
        BYDLog.e(TAG, "get: position size is " + position);
        return null;
    }

    /**
     * Remove and get the data.
     *
     * @param position
     * @return
     */
    public synchronized VideoCodeEncoder.MediaCodecData remove(int position) {
        // TODO Auto-generated method stub
        if (mFrameData != null && mFrameData.size() > position) {
            VideoCodeEncoder.MediaCodecData data = mFrameData.remove(position);
            mBufSize -= data.bufferInfo.size;
            return data;
        }
        BYDLog.e(TAG, "remove: mFrameData is " + mFrameData);
        if (mFrameData != null) {
            BYDLog.e(TAG, "remove: mFrameData size is " + mFrameData.size());
        }
        BYDLog.e(TAG, "remove: position size is " + position);
        return null;
    }

    /**
     * Get the frame data buffer size.
     *
     * @return
     */
    public synchronized int size() {
        if (mFrameData != null) {
            return mFrameData.size();
        }
        return 0;
    }

    /**
     * Clear the buffer.
     */
    public synchronized void clear() {
        mBufSize = 0;
        if (mFrameData != null) {
            mFrameData.clear();
        }
    }

    /**
     * Is null.
     *
     * @return
     */
    public synchronized boolean isEmpty() {
        // TODO Auto-generated method stub
        return mFrameData == null || mFrameData.size() == 0;
    }
}