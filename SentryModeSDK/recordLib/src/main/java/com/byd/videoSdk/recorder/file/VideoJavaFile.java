package com.byd.videoSdk.recorder.file;


import android.util.Log;


import com.byd.videoSdk.common.util.BYDLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;


/**
 * File util.
 *
 * @author bai.yu1
 */
public class VideoJavaFile {
    private static final String TAG = "VideoJavaFile";
    private RandomAccessFile mFile = null;
    private FileOutputStream mFOS = null;
    private String mFileName = null;

    public VideoJavaFile() {
    }

    /**
     * Open the file.
     * Must open then can write.
     *
     * @param name
     * @return
     */
    public boolean open(String name, String mode) {
        BYDLog.d(TAG, "open: E, name is " + name + " and mode is " + mode);
        if (name == null || name.isEmpty() || mode == null || mode.isEmpty()) {
            BYDLog.e(TAG, "open: input is null.");
            return false;
        }
        try {
            mFileName = name;
            //if (mode.equals("w")) {
            //    mFOS = new FileOutputStream(mFileName);
            //} else {
                mFile = new RandomAccessFile(name, mode);
            //}
            BYDLog.e(TAG, "open: X");
            return true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            if (mFile != null) {
                try {
                    mFile.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            BYDLog.e(TAG, "open: X, " + e);
        }
        return false;
    }

    /**
     * Close the file.
     * If close, the file can not be writed.
     */
    public void close() {
        BYDLog.e(TAG, "close: E ");
        try {
            if (mFile != null) {
                BYDLog.d(TAG, "close: mFileName = "+mFileName);
                mFile.close();
            } else if (mFOS != null) {
                mFOS.flush();
                mFOS.close();
            }
        } catch (IOException eio) {
            // TODO Auto-generated catch block
            BYDLog.e(TAG, "close: X, " + eio);
        } catch (Exception e) {
            BYDLog.e(TAG, "close:X, " + e);
        }
        mFile = null;
        BYDLog.e(TAG, "close: X ");
    }

    /**
     * Remove the file.
     *
     * @return
     */
    public boolean remove() {
        if (mFileName == null || mFileName.isEmpty()) {
            BYDLog.e(TAG, "remove: file name is null");
            return false;
        }
        File f = new File(mFileName);
        if (!f.exists()) {
            BYDLog.e(TAG, "remove: file[" + mFileName + "] is not exists");
            return false;
        }
        return f.delete();
    }

    /**
     * Is Exists or not..
     *
     * @return
     */
    public boolean exists() {
        if (mFileName == null || mFileName.isEmpty()) {
            BYDLog.w(TAG, "exists: name is " + mFileName);
            return false;
        }
        File f = new File(mFileName);
        if (f.exists()) {
            return true;
        }
        BYDLog.w(TAG, "exists: X, " + mFileName + " not exists");
        return false;
    }

    /**
     * get file pointer/
     * If "w" mode, will retuen Long.MAX_VALUE.
     *
     * @return
     */
    public long getFilePointer() {
        // TODO Auto-generated method stub
        try {
            if (mFile != null) {
                return mFile.getFilePointer();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Long.MAX_VALUE;
    }

    /**
     * Get len.
     *
     * @return
     */
    public long length() {
        if (mFileName == null || mFileName.isEmpty()) {
            BYDLog.w(TAG, "length: name is " + mFileName);
            return -1;
        }
        File f = new File(mFileName);
        if (!f.exists()) {
            BYDLog.w(TAG, "length: X, " + mFileName + " not exists");
            return -1;
        }
        return f.length();
    }

    /**
     * Get the file name
     *
     * @return
     */
    public String getName() {
        return mFileName;
    }

    /**
     * Get file channel
     *
     * @return
     */
    public FileChannel getChannel() {
        try {
            if (mFile != null) {
                return mFile.getChannel();
            } else if (mFOS != null) {
                return mFOS.getChannel();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * write int.
     *
     * @param data
     * @return
     */
    public boolean writeInt(int data) {
        try {
            if (mFile != null) {
                mFile.writeInt(data);
                return true;
            } else if (mFOS != null) {
                byte[] bData = new byte[4];
                //高位在前，写文件顺序
                bData[3] = (byte) (data & 0xff);
                bData[2] = (byte) ((data & 0xff00) >> 8);
                bData[1] = (byte) ((data & 0xff0000) >> 16);
                bData[0] = (byte) ((data & 0xff000000) >> 24);
                mFOS.write(bData);
                return true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * write long.
     *
     * @param data
     * @return
     */
    public boolean writeLong(long data) {
        try {
            if (mFile != null) {
                mFile.writeLong(data);
                return true;
            } else if (mFOS != null) {
                byte[] bData = new byte[8];
                //高位在前，写文件顺序
                bData[7] = (byte) (data & 0xff);
                bData[6] = (byte) ((data >> 8) & 0xff);
                bData[5] = (byte) ((data >> 16) & 0xff);
                bData[4] = (byte) ((data >> 24) & 0xff);
                bData[3] = (byte) ((data >> 32) & 0xff);
                bData[2] = (byte) ((data >> 40) & 0xff);
                bData[1] = (byte) ((data >> 48) & 0xff);
                bData[0] = (byte) ((data >> 56) & 0xff);
                mFOS.write(bData);
                return true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public boolean writeByteArray(int data) {
        try {
            if (mFile != null) {
                mFile.writeInt(data);
                return true;
            } else if (mFOS != null) {
                byte[] bData = new byte[4];
                //高位在前，写文件顺序
                bData[3] = (byte) (data & 0xff);
                bData[2] = (byte) ((data & 0xff00) >> 8);
                bData[1] = (byte) ((data & 0xff0000) >> 16);
                bData[0] = (byte) ((data & 0xff000000) >> 24);
                mFOS.write(bData);
                return true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Write data.
     *
     * @param buffer
     * @param byteOffset
     * @param byteCount
     * @return
     */
    public int write(byte[] buffer, int byteOffset, int byteCount) {
        try {
            if (mFile != null) {
                mFile.write(buffer, byteOffset, byteCount);
            } else if (mFOS != null) {
                mFOS.write(buffer, byteOffset, byteCount);
            } else {
                return -1;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        return byteCount;
    }

    /**
     * ReadInt
     *
     * @return
     * @throws IOException
     */
    public int readInt() throws IOException {
        if (mFile != null) {
            return mFile.readInt();
        } else {
            throw new IOException("File not opened.");
        }
    }

    /**
     * Read long
     *
     * @return
     * @throws IOException
     */
    public long readLong() throws IOException {
        if (mFile != null) {
            return mFile.readLong();
        } else {
            throw new IOException("File not opened.");
        }
    }


    /**
     * Read data.
     *
     * @param buffer
     * @param byteOffset
     * @param byteCount
     * @return
     */
    public int read(byte[] buffer, int byteOffset, int byteCount) {
        try {
            if (mFile != null) {
                return mFile.read(buffer, byteOffset, byteCount);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Flush data.
     *
     * @return
     */
    public boolean flush() {
        try {
            if (mFile != null) {
            } else if (mFOS != null) {
                mFOS.flush();
            } else {
                return false;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Sync the data to devices
     *
     * @return
     */
    public boolean sync() {
        try {
            if (mFile != null) {
                mFile.getFD().sync();
                Log.d(TAG, "sync: mFile");
            } else if (mFOS != null) {
                mFOS.getFD().sync();
                Log.d(TAG, "sync: mFOS");
            } else {
                return false;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
