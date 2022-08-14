package com.byd.videoSdk.recorder.file;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;


import com.byd.videoSdk.recorder.utils.SDKMediaFormat;
import com.byd.videoSdk.common.util.BYDLog;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.SyncFailedException;
import java.nio.ByteBuffer;


/**
 * File interface for mpeg4.
 *
 * @author bai.yu1
 */
public class VideoMpeg4File {
    private static final String TAG = "VideoMpeg4File";
    private boolean mIsSync = false;
    private String mFileName = null;
    private RandomAccessFile mFile = null;
    private MediaMuxer mMediaMuxer = null;
    private int mVideoTrackIndex = -1;
    private MediaFormat mVideoFormat = null;

    public VideoMpeg4File(String name) throws IOException {
        mIsSync = false;
        if (!open(name)) {
            throw new IOException("The file " + name + " open fail");
        }
    }

    public VideoMpeg4File(String name, boolean sync) throws IOException {
        mIsSync = sync;
        if (!open(name)) {
            throw new IOException("The file " + name + " open fail");
        }
    }

    /**
     * Open the file.
     * Must open then can write.
     *
     * @param name is Absolute path and name
     * @return
     * @throws FileNotFoundException
     */
    private synchronized boolean open(String name) {
        if (name == null || name.isEmpty()/* || !name.endsWith(".mp4")*/) {
            BYDLog.e(TAG, "openFile: name[" + name + "] is err");
            return false;
        }
        mFileName = name;
        BYDLog.d(TAG, "open mFileName : " + mFileName);
        try {
            mFile = new RandomAccessFile(mFileName, "rws");
            mFile.setLength(0);
            FileDescriptor fd = mFile.getFD();
            mMediaMuxer = new MediaMuxer(name, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            BYDLog.e(TAG, "open:" + e);
        }
        return false;
    }

    /**
     * Get the file name.
     *
     * @return
     */
    public String getName() {
        return mFileName;
    }

    /**
     * Close the file.
     * If close, the file can not be writed.
     */
    public synchronized void close() {
        BYDLog.d(TAG, "close mFileName : " + mFileName);
        try {
            muxerStop();
            if (mFile != null) {
                if (mIsSync) {
                    FileDescriptor fd = mFile.getFD();
                    if (fd != null) {
                        fd.sync();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (mFile != null) {
                    BYDLog.d(TAG, "close mFile!");
                    mFile.close();
                    mFile = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mFileName = null;
        mMediaMuxer = null;
    }

    private synchronized void muxerStop() {
        BYDLog.d(TAG, "muxerStop : " + mFileName);
        try {
            try {
                if (mMediaMuxer != null) {
                    mMediaMuxer.stop();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            try {
                if (mMediaMuxer != null) {
                    mMediaMuxer.release();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        mMediaMuxer = null;
    }

    public synchronized boolean sync() {
        BYDLog.v(TAG, "sync: E, mFile " + mFile);
        if (mFile != null) {
            try {
                FileDescriptor fd = mFile.getFD();
                if (fd != null) {
                    fd.sync();
                    BYDLog.v(TAG, "sync: X, OK sync mFile " + mFile);
                    return true;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        BYDLog.e(TAG, "sync: X, ERR sync mFile " + mFile);
        return false;
    }

    /**
     * Set the mediaformat
     *
     * @param f
     * @return
     */
    public synchronized boolean setVideoMediaFormat(MediaFormat f) {
        if (!setFormat(f)) {
            BYDLog.e(TAG, "setMediaFormat: error");
            return false;
        }
        if(mMediaMuxer!=null) {
            mMediaMuxer.start();
        }
        return true;
    }


    private synchronized boolean setFormat(MediaFormat mediaFormat) {
        if (mediaFormat == null) {
            BYDLog.e(TAG, "setMediaFormat: input id null");
            return false;
        }
        BYDLog.d(TAG, "setVideoMediaFormat: mFileName = "+mFileName);
        SDKMediaFormat.printByteBuffer(mediaFormat, "csd-0");
        SDKMediaFormat.printByteBuffer(mediaFormat, "csd-1");
        BYDLog.i(TAG, "setMediaFormat() mediaFormat = " + mediaFormat);

        mVideoTrackIndex = mMediaMuxer.addTrack(mediaFormat);
        if (mVideoTrackIndex < 0) {
            BYDLog.e(TAG, "setMediaFormat: add video track fail");
            return false;
        }
        mVideoFormat = mediaFormat;
        return true;
    }

    /**
     * Write the video frame to file.
     *
     * @param buffer
     * @param info
     * @return
     */
    public synchronized boolean writeSampeData(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        if (buffer == null || info == null) {
            BYDLog.e(TAG, "writeSampeData: input params null");
            return false;
        }
        writeFrameData(buffer, info);
        try {
            if (mFile != null && mIsSync) {
                FileDescriptor fd = mFile.getFD();
                if (fd != null) {
                    fd.sync();
                }
            }
        } catch (SyncFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    private synchronized boolean writeFrameData(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        if (buffer == null || info == null) {
            BYDLog.e(TAG, "writeSampeData: input params null");
            return false;
        }
        if (mVideoFormat == null) {
            BYDLog.e(TAG, "writeSampeData: mMediaFormat null");
            return false;
        }
        if (mVideoTrackIndex < 0) {
            BYDLog.e(TAG, "writeSampeData: mVideoTrackIndex is " + mVideoTrackIndex);
            return false;
        }
        if (mMediaMuxer == null) {
            BYDLog.e(TAG, "writeSampeData: mMediaMuxer is " + mMediaMuxer);
            return false;
        }
        try {
            Log.d(TAG, "writeFrameData: "+ mFileName);
            mMediaMuxer.writeSampleData(mVideoTrackIndex, buffer, info);
        } catch (Exception e) {
            BYDLog.e(TAG, "writeSampeData error");
            return false;
        }
        return true;
    }

    /**
     * Exists.
     *
     * @param name
     * @return
     */
    public static boolean exists(String name) {
        if (name == null || name.isEmpty()) {
            BYDLog.e(TAG, "openFile: name is null");
            return false;
        }
        File f = new File(name);
        return f.exists();
    }

    /**
     * Get the length of the file.
     *
     * @param name
     * @return
     */
    public static long getLength(String name) {
        if (name == null || name.isEmpty()) {
            BYDLog.e(TAG, "openFile: name is null");
            return 0;
        }
        File f = new File(name);
        if (f.exists()) {
            return f.length();
        }
        return 0;
    }

    /**
     * Create the file.
     *
     * @return
     */
    public static boolean create(String name) {
        if (name == null || name.isEmpty()) {
            BYDLog.e(TAG, "create: name is null");
            return false;
        }
        File f = new File(name);
        try {
            return f.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            BYDLog.e(TAG, "create", e);
        }
        return false;
    }

    /**
     * Remove the file.
     *
     * @param name
     * @return
     */
    public static boolean remove(String name) {
        if (name == null || name.isEmpty()) {
            BYDLog.e(TAG, "remove: name is null");
            return false;
        }
        File f = new File(name);
        return f.delete();
    }

    /**
     * Rename the file.
     *
     * @param oldName
     * @param newName
     * @return
     */
    public static boolean rename(String oldName, String newName) {
        if (oldName == null || oldName.isEmpty()) {
            BYDLog.e(TAG, "remove: oldname is null");
            return false;
        }
        if (newName == null || newName.isEmpty()) {
            BYDLog.e(TAG, "remove: new name is null");
            return false;
        }
        File f = new File(oldName);
        File newFile = new File(newName);
        return f.renameTo(newFile);
    }

    /**
     * Check has or not the mimeType
     *
     * @param mimeType
     * @return
     * @throws IOException
     */
    public static boolean check(String name, String mimeType) {
        BYDLog.d(TAG, "check: E, name is " + name + ", mime is " + mimeType);
        if (name == null || mimeType == null || name.isEmpty() || mimeType.isEmpty()) {
            BYDLog.w(TAG, "check: input is null");
            return false;
        }
        File f = new File(name);
        if (!f.exists()) {
            BYDLog.w(TAG, "openFile: file[" + name + "] is not exsits.");
            return false;
        }
        MediaExtractor mMediaExtractor = new MediaExtractor();
        try {
            mMediaExtractor.setDataSource(name);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            BYDLog.e(TAG, "openFile:", e);
            try {
                if (mMediaExtractor != null) {
                    mMediaExtractor.release();
                    mMediaExtractor = null;
                }
            } catch (Exception e1) {
                BYDLog.e(TAG, "check: mMediaExtractor release ", e1);
            }
            return false;
        }
        boolean isFound = false;
        try {
            BYDLog.d(TAG, "check: getTrackCount is " + mMediaExtractor.getTrackCount());
            int index = -1;
            if (mMediaExtractor.getTrackCount() <= 0) {
                BYDLog.w(TAG, "check: not found track count.");
                isFound = false;
            } else {
                for (index = 0; index < mMediaExtractor.getTrackCount(); index++) {
                    MediaFormat format = mMediaExtractor.getTrackFormat(index);
                    String mime = format.getString(MediaFormat.KEY_MIME);

                    BYDLog.d(TAG, "check: mime is " + mime);
                    if (mime.equals(mimeType)) {
                        isFound = true;
                        break;
                    }
                }
                if (isFound && index >= 0) {
                    mMediaExtractor.selectTrack(index);
                    MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(index);
                    if (mediaFormat == null) {
                        BYDLog.w(TAG, "check: not found the mediaformat.");
                        isFound = false;
                    } else {
                        ByteBuffer buffer = ByteBuffer.allocate(512 * 1024);
                        int sampleSize = mMediaExtractor.readSampleData(buffer, 0);
                        if (sampleSize < 0) {
                            BYDLog.w(TAG, "check: sampleSize is " + sampleSize);
                            isFound = false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            BYDLog.e(TAG, "check: X, err", e);
        }
        try {
            if (mMediaExtractor != null) {
                mMediaExtractor.release();
                mMediaExtractor = null;
            }
        } catch (Exception e) {
            BYDLog.e(TAG, "check: X, err", e);
        }
        BYDLog.d(TAG, "check: X, found " + isFound);
        return isFound;
    }
}
