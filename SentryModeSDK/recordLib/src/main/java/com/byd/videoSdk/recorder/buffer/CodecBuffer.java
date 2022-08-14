package com.byd.videoSdk.recorder.buffer;

import android.media.MediaCodec;
import android.media.MediaFormat;


import com.byd.videoSdk.recorder.encodec.VideoCodeEncoder;
import com.byd.videoSdk.common.util.BYDLog;

import java.util.ArrayList;
import java.util.Vector;

/**
 * The Codec buffer.
 * the buffer is vector<vector<MediaCodecData>>:
 * A subclass DataBuffer is used save a vector<MediaCodecData> whose data is <IFrame, Pframe ......... PFrame>;
 * And the the codec buffer is save Vector<DataBuffer>;
 * <p>
 * The buffer is:
 * IFrame, Pframe ......... PFrame;
 * ...............................;
 * IFrame, Pframe ......... PFrame;
 * <p>
 * MediaCodecData is a I/P frame from MediaCodec.
 */
public class CodecBuffer {
    private static final String TAG = "CodecBuffer";
    private static final boolean DEBUG = false;
    //Save the data buffer.
    private long mBufferSize = 0;//Not used ??
    private long mMediaCodecDataSize = 0;//Not used ??
    private Vector<DataBuffer> mCodecDatas;
    private MediaFormat mCurMediaFormat = null;//Current media format
    private long mBufferTimeLenght = -1;

    /**
     * The buffer will be not limited the time.
     */
    public CodecBuffer() {
        mBufferTimeLenght = -1;
        mCodecDatas = new Vector<DataBuffer>();
    }

    /**
     * Vector is initial capacity and capacity Increment.
     *
     * @param initialcapacity
     * @param capacityIncrement
     */
    public CodecBuffer(int initialcapacity, int capacityIncrement) {
        mBufferTimeLenght = -1;
        mCodecDatas = new Vector<DataBuffer>(initialcapacity, capacityIncrement);
    }

    /**
     * If the lastDataBufferTime - firstDataBufferTime > timeLen,
     * the first data buffer will be removed.
     * Vector is initial capacity and capacity Increment.
     *
     * @param timeLen
     * @param initialcapacity
     * @param capacityIncrement
     */
    public CodecBuffer(long timeLen, int initialcapacity, int capacityIncrement) {
        mBufferTimeLenght = timeLen;
        mCodecDatas = new Vector<DataBuffer>(initialcapacity, capacityIncrement);
    }

    /**
     * Return the data buffer count.
     *
     * @return
     */
    public synchronized long sizeOfDataBuffer() {
        return mCodecDatas.size();
    }

    /**
     * The media codec buffer count.
     *
     * @return
     */
    public synchronized long sizeOfMediaCodecData() {
        return mMediaCodecDataSize;
    }

    /**
     * The all buffer sizes.
     *
     * @return
     */
    public synchronized long sizeOfBuffer() {
        return mBufferSize;
    }

    /**
     * The data is null.
     *
     * @return
     */
    public synchronized boolean isEmpty() {
        return mCodecDatas.size() == 0;
    }

    /**
     * Get the index data buffer.
     *
     * @param index
     * @return
     */
    public synchronized DataBuffer getDataBuffer(int index) {
        if (mCodecDatas != null && mCodecDatas.size() > index) {
            return mCodecDatas.get(index);
        }
        return null;
    }

    /**
     * Remove the index data buffer.
     * If the index is the last, maybe the data buffer is not include all PFrame of the IFrame.
     *
     * @param index
     */
    public synchronized DataBuffer removeDataBuffer(int index) {
        if (mCodecDatas != null && mCodecDatas.size() > index) {
            DataBuffer db = mCodecDatas.remove(index);
            if (db != null && db.size() > 0) {
                for (int position = 0; position < db.size(); position++) {
                    VideoCodeEncoder.MediaCodecData d = db.getMediaCodecData(position);
                    if (d == null) continue;
                    mBufferSize = d.bufferInfo.size;
                }
                mMediaCodecDataSize -= db.size();
            }
            return mCodecDatas.remove(index);
        }
        return null;
    }

    /**
     * Remove alldata buffer from vector and return the data buffer.
     *
     * @return Vector<DataBuffer>, need free by caller.
     */
    public synchronized Vector<DataBuffer> clearAndGet() {
        if (mCodecDatas != null) {
            Vector<DataBuffer> tmp = mCodecDatas;
            mCodecDatas = new Vector<DataBuffer>(20, 5);
            mBufferSize = 0;
            mMediaCodecDataSize = 0;
            return tmp;
        }
        return null;
    }

    /**
     * clear the data buffer from vector and clear data buffer data.
     * Clear all data.
     */
    public synchronized void clear() {
        if (mCodecDatas != null) {
            for (int i = 0; i < mCodecDatas.size(); i++) {
                DataBuffer d = mCodecDatas.get(i);
                d.clear();
                d = null;
            }
            mCodecDatas.clear();
            mBufferSize = 0;
            mMediaCodecDataSize = 0;
        }
    }

    /**
     * Set the media format
     *
     * @param mediaFormat
     */
    public synchronized void setMediaFormat(MediaFormat mediaFormat) {
        mCurMediaFormat = mediaFormat;
    }

    /**
     * Get the MediaFormat.
     *
     * @return
     */
    public synchronized MediaFormat getMediaFormat() {
        return mCurMediaFormat;
    }

    /**
     * Get the first data buffer time(IFrame time).
     *
     * @return
     */
    public synchronized long getFirstTime() {
        if (mCodecDatas != null) {
            DataBuffer db = mCodecDatas.get(0);
            if (db != null) {
                return db.getTime();
            }
        }
        return -1;
    }

    /**
     * Get the last data buffer time(IFrame time).
     *
     * @return
     */
    public synchronized long getLirstTime() {
        if (mCodecDatas != null) {
            DataBuffer db = mCodecDatas.get(mCodecDatas.size() - 1);
            if (db != null) {
                return db.getTime();
            }
        }
        return -1;
    }

    /**
     * Copy the MediaCodecData and Saved.
     *
     * @param data
     * @return the copyed data buffer that saved to the codec buffer.
     */
    public synchronized VideoCodeEncoder.MediaCodecData saveDataToBuffer(VideoCodeEncoder.MediaCodecData data) {
        BYDLog.v(TAG, "saveDataToBuffer: E");
        if (mCodecDatas == null) {
            BYDLog.e(TAG, "saveDataToBuffer: the buffer is not create.");
            return null;
        }

        if (data.bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
            BYDLog.v(TAG, "saveDataToBuffer: is I frame");
            DataBuffer buf = new DataBuffer(15, 5);
            buf.addMediaCodecData(data);
            mBufferSize += data.bufferInfo.size;
            buf.setTime(data.bufferInfo.presentationTimeUs);
            mCodecDatas.add(buf);
            BYDLog.v(TAG, "mCodecDatas.size = " + mCodecDatas.size());
            DataBuffer bufo = mCodecDatas.get(0);
            BYDLog.v(TAG, "curtime = " + buf.getTime());
            BYDLog.v(TAG, "lasttime = " + bufo.getTime());
            BYDLog.v(TAG, "durning time = " + (buf.getTime()-bufo.getTime()));
            if (mBufferTimeLenght > 0) {
                while ((bufo != null) && ((buf.getTime() - bufo.getTime()) > mBufferTimeLenght)) {
                    BYDLog.v(TAG, "saveDataToBuffer: will remove the old data");
                    DataBuffer b0 = mCodecDatas.remove(0);
                    if (b0 != null) {
                        mBufferSize -= b0.getBufSize();
                        b0.clear();
                    }
                    bufo = mCodecDatas.get(0);
                }
            }
            BYDLog.v(TAG, "mBufferSize = " + mBufferSize);
            BYDLog.v(TAG, "mCodecDatas.size() = " + mCodecDatas.size());
            BYDLog.v(TAG, "saveDataToBuffer: X");
            return data;
        } else {
            //OUSHANGLog.d(TAG, "saveDataToBuffer: is not I frame");
            if (mCodecDatas.size() > 0) {
                DataBuffer buf = mCodecDatas.get(mCodecDatas.size() - 1);
                if (buf != null) {
                    buf.addMediaCodecData(data);
                    mBufferSize += data.bufferInfo.size;
                    BYDLog.v(TAG, "saveDataToBuffer: X");
                    return data;
                }
            } else {
                BYDLog.d(TAG, "saveDataToBuffer: is not I frame and not data now, not save it.");
            }

            return null;
        }
    }

    /**
     * Copy data buffer to a vector.
     *
     * @param bufCopy
     * @return
     */
    public synchronized boolean copyDataBuffer(ArrayList<DataBuffer> bufCopy) {
        BYDLog.d(TAG, "copyBuffer: E");
        if (mCodecDatas == null) {
            BYDLog.e(TAG, "codec data buffer is null");
            return false;
        }
        if (bufCopy == null) {
            BYDLog.e(TAG, "CodecBufferCopy is null");
            return false;
        }
        if (DEBUG) {
            BYDLog.d(TAG, "mCodecDatas.size() = " + mCodecDatas.size());
        }
        for (int i = 0; i < mCodecDatas.size(); i++) {
            DataBuffer dataBuf = mCodecDatas.get(i);
            if (dataBuf == null) {
                BYDLog.e(TAG, "saved databuffer is null!");
                continue;
            }
            bufCopy.add(dataBuf);
            BYDLog.v(TAG, "dataBuf.size() = " + dataBuf.size());
            if (dataBuf != null && dataBuf.size() > 0) {
                for (int j = 0; j < dataBuf.size(); j++) {
                    VideoCodeEncoder.MediaCodecData codecData = dataBuf.getMediaCodecData(j);
                    if (codecData == null) {
                        continue;
                    }
                    if (DEBUG) {
                        BYDLog.v(TAG, "buffer info size = " + codecData.bufferInfo.size);
                        BYDLog.v(TAG, "buffer info flags = " + codecData.bufferInfo.flags);
                    }
                    BYDLog.v(TAG, "copy buffer time = " + codecData.bufferInfo.presentationTimeUs);
                }
            }
        }
        BYDLog.d(TAG, "copyAndClearBuffer: X");
        return true;
    }

    /**
     * Copy MediaCodecData to a vector.
     *
     * @param bufCopy
     * @return
     */
    public synchronized boolean copyMediaCodecData(ArrayList<VideoCodeEncoder.MediaCodecData> bufCopy) {
        BYDLog.d(TAG, "copyBuffer: E");
        if (mCodecDatas == null) {
            BYDLog.e(TAG, "codec data buffer is null");
            return false;
        }
        if (bufCopy == null) {
            BYDLog.e(TAG, "CodecBufferCopy is null");
            return false;
        }
        if (DEBUG) {
            BYDLog.d(TAG, "mCodecDatas.size() = " + mCodecDatas.size());
        }
        for (int i = 0; i < mCodecDatas.size(); i++) {
            DataBuffer dataBuf = mCodecDatas.get(i);
            if (dataBuf == null) {
                BYDLog.e(TAG, "saved databuffer is null!");
                continue;
            }
            BYDLog.v(TAG, "dataBuf.size() = " + dataBuf.size());
            if (dataBuf != null && dataBuf.size() > 0) {
                for (int j = 0; j < dataBuf.size(); j++) {
                    VideoCodeEncoder.MediaCodecData codecData = dataBuf.getMediaCodecData(j);
                    if (codecData == null) {
                        continue;
                    }
                    if (DEBUG) {
                        BYDLog.v(TAG, "buffer info size = " + codecData.bufferInfo.size);
                        BYDLog.v(TAG, "buffer info flags = " + codecData.bufferInfo.flags);
                    }
                    BYDLog.v(TAG, "copy buffer time = " + codecData.bufferInfo.presentationTimeUs);
                    bufCopy.add(codecData);
                }
            }
        }
        BYDLog.d(TAG, "copyAndClearBuffer: X");
        return true;
    }
}
