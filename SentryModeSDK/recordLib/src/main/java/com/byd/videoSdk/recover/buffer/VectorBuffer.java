package com.byd.videoSdk.recover.buffer;


import com.byd.videoSdk.common.util.BYDLog;
import com.byd.videoSdk.recorder.utils.ByteUtil;

public class VectorBuffer {
    private static final String TAG = "VectorBuffer";
    private byte[] mBytes = null;
    private int mValidLen = 0;
    private int mReadPosition = 0;
    private int mWritePosition = 0;
    private int mNumber;
    private int mInitCapacity;
    private boolean mLockStatus = false;

    public VectorBuffer(int initCapacity) {
        mInitCapacity = initCapacity;
        mBytes = new byte[initCapacity];
        mValidLen = 0;
        mReadPosition = 0;
        mWritePosition = 0;
    }

    public synchronized boolean isLocked() {
        return mLockStatus;
    }

    public synchronized void lock() {
        mLockStatus = true;
    }

    public synchronized void unLock() {
        mLockStatus = false;
    }

    public synchronized int putByte(byte data) {
        if (mLockStatus) {
            BYDLog.w(TAG, "put: is locked.");
            return 0;
        }
        if (mBytes.length - mValidLen < 1) {
            byte[] newBuf = new byte[mBytes.length + 1];
            System.arraycopy(mBytes, 0, newBuf, 0, mValidLen);
            mBytes = newBuf;
        }
        //System.arraycopy(data, offset, mBytes, mValidLen, length);
        mBytes[mValidLen] = data;
        mValidLen += 1;
        mWritePosition += 1;
        mNumber++;
        if (mNumber > 90) {
            BYDLog.w(TAG, "put: frame number is " + mNumber);
            BYDLog.w(TAG, "put: frame mValidLen is " + mValidLen);
        }
        return 1;
    }

    /**
     * Put a int data.
     *
     * @param data
     * @return
     */
    public synchronized int putInt(int data) {
        return put(ByteUtil.getBytes(data), 0, Integer.SIZE / 8);
    }

    /**
     * Put a long data.
     *
     * @param data
     * @return
     */
    public synchronized int putLong(long data) {
        return put(ByteUtil.getBytes(data), 0, Long.SIZE / 8);
    }

    /**
     * Put a byte buffer.
     *
     * @param data
     * @param offset
     * @param length
     * @return
     */
    public synchronized int put(byte[] data, int offset, int length) {
        if (mLockStatus) {
            BYDLog.w(TAG, "put: is locked.");
            return 0;
        }
        if (data == null || length <= 0 || offset < 0) {
            BYDLog.w(TAG, "put: input err.");
            return 0;
        }
        if (mBytes.length - mValidLen < length) {
            byte[] newBuf = new byte[mBytes.length + length];
            System.arraycopy(mBytes, 0, newBuf, 0, mValidLen);
            mBytes = newBuf;
        }
        System.arraycopy(data, offset, mBytes, mValidLen, length);
        mValidLen += length;
        mWritePosition += length;
        mNumber++;
        if (mNumber % 30 == 0) {
            BYDLog.v(TAG, "put: frame number is " + mNumber);
            BYDLog.v(TAG, "put: frame mValidLen is " + mValidLen);
        }
        return data.length;
    }

    /**
     * get data.
     * Len need get getValidLen.
     *
     * @return
     */
    public synchronized byte[] get() {
        return mBytes;
    }

    /**
     * Get data and the read postion will add.
     *
     * @return
     * @throws Exception
     */
    public synchronized int getIntAndRM() throws Exception {
        if (mValidLen > 0 && mValidLen >= Integer.SIZE / 8
                && mReadPosition <= mWritePosition - 4) {
            int data = (0xff & mBytes[mReadPosition])
                    | (0xff00 & (mBytes[mReadPosition + 1] << 8))
                    | (0xff0000 & (mBytes[mReadPosition + 2] << 16))
                    | (0xff000000 & (mBytes[mReadPosition + 3] << 24));
            mReadPosition += 4;
            mValidLen -= 4;
            return data;
        }
        throw new Exception("buffer is not enough.");
    }

    /**
     * Get data and the read postion will add.
     *
     * @return
     * @throws Exception
     */
    public synchronized long getLongAndRM() throws Exception {
        if (mValidLen > 0 && mValidLen >= Long.SIZE / 8
                && mReadPosition <= mWritePosition - 8) {
            long data = (0xffL & (long) mBytes[mReadPosition])
                    | (0xff00L & ((long) mBytes[mReadPosition + 1] << 8))
                    | (0xff0000L & ((long) mBytes[mReadPosition + 2] << 16))
                    | (0xff000000L & ((long) mBytes[mReadPosition + 3] << 24))
                    | (0xff00000000L & ((long) mBytes[mReadPosition + 4] << 32))
                    | (0xff0000000000L & ((long) mBytes[mReadPosition + 5] << 40))
                    | (0xff000000000000L & ((long) mBytes[mReadPosition + 6] << 48))
                    | (0xff00000000000000L & ((long) mBytes[mReadPosition + 7] << 56));
            mReadPosition += 8;
            mValidLen -= 8;
            return data;
        }
        throw new Exception("buffer is not enough.");
    }

    /**
     * Get data and the read postion will add.
     *
     * @return
     * @throws Exception
     */
    public synchronized byte getByteAndRM() throws Exception {
        if (mValidLen > 0 && mValidLen >= 1
                && mReadPosition <= mWritePosition - 1) {
            byte b = mBytes[mReadPosition];
            mReadPosition += 1;
            mValidLen -= 1;
            return b;
        }
        throw new Exception("buffer is not enough.");
    }

    /**
     * Get data and the read postion will add.
     *
     * @return
     * @throws Exception
     */
    public synchronized byte[] getByteArrayAndRM(int length) throws Exception {
        if (mValidLen > 0 && mValidLen >= length
                && mReadPosition <= mWritePosition - length) {
            byte[] bdata = new byte[length];
            System.arraycopy(mBytes, mReadPosition, bdata, 0, length);
            mReadPosition += length;
            mValidLen -= length;
            return bdata;
        }
        throw new Exception("buffer is not enough.");
    }

    /**
     * Get data and the read postion will add.
     *
     * @return
     * @throws Exception
     */
    public synchronized int getByteArrayAndRM(byte[] bytes, int length) throws Exception {
        if(bytes == null || bytes.length < length){
            return -1;
        }
        if (mValidLen > 0 && mValidLen >= length
                && mReadPosition <= mWritePosition - length) {
            System.arraycopy(mBytes, mReadPosition, bytes, 0, length);
            mReadPosition += length;
            mValidLen -= length;
            return length;
        }
        throw new Exception("buffer is not enough.");
    }
    /**
     * Get valid data len.
     *
     * @return
     */
    public synchronized int getValidLen() {
        return mValidLen;
    }

    /**
     * Clear buffer
     */
    public synchronized boolean clear() {
        BYDLog.v(TAG, "clear: E");
        if (mLockStatus) {
            BYDLog.w(TAG, "clear: is locked.");
            return false;
        }
        mValidLen = 0;
        mNumber = 0;
        mReadPosition = 0;
        mWritePosition = 0;
        if (mBytes == null && mBytes.length > mInitCapacity) {
            mBytes = new byte[mInitCapacity];
        }
        return true;
    }
}