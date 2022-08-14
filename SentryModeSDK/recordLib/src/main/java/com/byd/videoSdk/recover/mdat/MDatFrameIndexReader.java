package com.byd.videoSdk.recover.mdat;

import android.annotation.SuppressLint;
import android.media.MediaCodec.BufferInfo;


import com.byd.videoSdk.recorder.file.VideoJavaFile;
import com.byd.videoSdk.common.util.BYDLog;

import java.io.IOException;

/**
 * Reader for h264 from file.
 *
 * @author bai.yu1
 */
@SuppressLint("NewApi")
public class MDatFrameIndexReader extends VideoJavaFile {
    private static final String TAG = "MDatFrameIndexReader";
    //private RandomAccessFile mFile = null;

    public MDatFrameIndexReader(String name) throws IOException {
        super();
        if (!openFile(name)) {
            throw new IOException("The file " + name + " open fail");
        }
    }

    /**
     * Open the file.
     * Must open then can write.
     *
     * @param name
     * @return
     */
    private synchronized boolean openFile(String name) {
        if (super.open(name, "r")) {
            //mFile = super.getFile();
            return true;
        }
        return false;
    }

    /**
     * Close the file.
     * If close, the file can not be writed.
     */
    public synchronized void closeFile() {
        //mFile = null;
        super.close();
    }

    /**
     * Read data from file.
     *
     * @param bufferInfo
     * @return the data
     */
    public boolean read(BufferInfo bufferInfo) {
        BYDLog.v(TAG, "read: E.");
        if (bufferInfo == null) {
            BYDLog.e(TAG, "read: X, err input is null.");
            return false;
        }
        /* Write data:
         * mFile.writeInt(bufferInfo.offset);
         * mFile.writeInt(bufferInfo.size);
         * mFile.writeLong(bufferInfo.presentationTimeUs);
         * mFile.writeInt(bufferInfo.flags);
         */
        try {
            if ((length() - getFilePointer()) < (Integer.SIZE * 3 + Long.SIZE)/8) {
                BYDLog.w(TAG, "read: X, file len is err.");
                return false;
            }
            BYDLog.v(TAG, "read: getFilePointer() is " + getFilePointer());
            bufferInfo.offset = readInt();
            BYDLog.v(TAG, "read: offset is " + bufferInfo.offset);
            bufferInfo.size = readInt();
            BYDLog.v(TAG, "read: size is " + bufferInfo.size);
            bufferInfo.presentationTimeUs = readLong();
            BYDLog.v(TAG, "read: presentationTimeUs is " + bufferInfo.presentationTimeUs);
            bufferInfo.flags = readInt();
            BYDLog.v(TAG, "read: flags is " + bufferInfo.flags);
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            BYDLog.e(TAG, "read: X, err and " + e);
        }
        BYDLog.v(TAG, "read: X.");
        return false;
    }
}
