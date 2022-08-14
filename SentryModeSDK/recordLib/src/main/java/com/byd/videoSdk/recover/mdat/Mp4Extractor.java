package com.byd.videoSdk.recover.mdat;

import android.util.Log;


import com.byd.videoSdk.recover.mdat.box.Box;
import com.byd.videoSdk.recover.mdat.box.FileTypeBox;
import com.byd.videoSdk.recover.mdat.box.FreeSpaceBox;
import com.byd.videoSdk.recover.mdat.box.MediaDataBox;
import com.byd.videoSdk.recover.mdat.box.MovieBox;
import com.byd.videoSdk.common.util.BYDLog;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * @author bai.yu1
 */
public class Mp4Extractor {
    private static final String TAG = "Mp4Extractor";
    private FileTypeBox mFtyhBox;
    private FreeSpaceBox mFreeBox;
    private MovieBox mMoovBox;
    private ArrayList<MediaDataBox> mMdatBoxs = new ArrayList<MediaDataBox>();
    private ArrayList<Box> mBoxs = new ArrayList<Box>();
    private OnAnalysisListener mOnAnalysisListener;
    private File mMp4File = null;

    private Mp4Extractor() {
        mOnAnalysisListener = new OnAnalysisListener() {
            @Override
            public void onAnalysis(Box box) {
                // TODO Auto-generated method stub
                if (box == null) return;
                box.dump();
                if (Box.BTYPE_MDAT.equals(box.getType())
                        && box instanceof MediaDataBox) {
                    mMdatBoxs.add((MediaDataBox) box);
                } else if (Box.BTYPE_MOOV.equals(box.getType())
                        && box instanceof MovieBox) {
                    mMoovBox = (MovieBox) box;
                } else if (Box.STYPE_FREE.equals(box.getType())
                        && box instanceof FreeSpaceBox) {
                    mFreeBox = (FreeSpaceBox) box;
                } else if (Box.STYPE_FTYH.equals(box.getType())
                        && box instanceof FileTypeBox) {
                    mFtyhBox = (FileTypeBox) box;
                } else {
                    mBoxs.add(box);
                }
            }
        };
    }

    public static Mp4Extractor create(File f) {
        if (f == null || !f.exists()) {
            return null;
        }
        Mp4Extractor mMp4Extractor = new Mp4Extractor();
        mMp4Extractor.open(f);
        return mMp4Extractor;
    }

    private boolean open(File f) {
        if (f == null || !f.exists()) {
            return false;
        }
        mMp4File = f;
        return analysis(mMp4File);
    }

    public void free() {
        mMp4File = null;
        mFtyhBox = null;
        mFreeBox = null;
        mMoovBox = null;
        mOnAnalysisListener = null;
        mMdatBoxs.clear();
        mBoxs.clear();

    }

    /**
     * Find MDataBox from file.
     *
     * @param in
     * @return
     */
    private boolean analysis(File in) {
        if (in == null) {
            return false;
        }
        boolean success = false;
        DataInputStream dIn = null;
        try {
            dIn = new DataInputStream(new FileInputStream(in));
            success = analysis(dIn, mOnAnalysisListener);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (dIn != null) {
                try {
                    dIn.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                dIn = null;
            }
        }
        return success;
    }

    /**
     * Analysis a file.
     *
     * @author bai.yu1
     */
    public static interface OnAnalysisListener {
        public void onAnalysis(Box box);
    }

    /**
     * Find MData box from stream.
     *
     * @param dIn
     * @return
     */
    public static boolean analysis(DataInputStream dIn, OnAnalysisListener l) {
        if (dIn == null) {
            return false;
        }
        long largeSize = -1;
        int size = -1;
        byte[] type = new byte[4];
        String strType = null;
        byte[] data = new byte[1024];
        long offset = 0;
        int boxLen = 0;
        int retLen = 0;
        try {
            while (true) {
                retLen = 0;
                boxLen = 0;
                largeSize = -1;
                size = -1;
                BYDLog.e(TAG, "analysis: offset is " + offset);
                //read size
                size = dIn.readInt();
                if (size <= 0) {//Err
                    BYDLog.e(TAG, "analysis: read size is " + size);
                    return false;
                }
                boxLen = 4;
                BYDLog.d(TAG, "analysis: size is " + size);
                BYDLog.d(TAG, "analysis: boxLen is " + boxLen);
                //read type
                retLen = dIn.read(type);
                BYDLog.d(TAG, "analysis: type is {" + (char) type[0] + "," + (char) type[1] + "," + (char) type[2] + "," + (char) type[3] + "}");
                strType = null;
                strType = "" + (char) type[0] + (char) type[1] + (char) type[2] + (char) type[3];
                BYDLog.d(TAG, "analysis: str type is " + strType);
                if (retLen != type.length) {
                    Log.e(TAG, "analysis: read len is " + retLen);
                    return false;
                }
                boxLen += retLen;
                BYDLog.d(TAG, "analysis: boxLen is " + boxLen);
                //read largeSize
                if (size == 1) {
                    largeSize = dIn.readLong();
                    BYDLog.d(TAG, "analysis: largeSize is " + largeSize);
                    boxLen += 8;
                    BYDLog.d(TAG, "analysis: boxLen is " + boxLen);
                }
                BYDLog.d(TAG, "mdat: " + Box.STYPE_MDAT.equals(strType));
                BYDLog.d(TAG, "moov: " + Box.STYPE_MOOV.equals(strType));
                BYDLog.d(TAG, "free: " + Box.STYPE_FREE.equals(strType));
                BYDLog.d(TAG, "ftyp: " + Box.STYPE_FTYH.equals(strType));
                //init box
                if (Box.STYPE_MDAT.equals(strType)) {
                    MediaDataBox box = new MediaDataBox();
                    box.setType(type, strType);
                    box.setSize(size);
                    box.setLargeSize(largeSize);
                    box.setOffset(offset);
                    if (l != null) {
                        l.onAnalysis(box);
                    }
                    //mMdatBoxs.add(box);
                } else if (Box.STYPE_MOOV.equals(strType)) {
                    MovieBox box = new MovieBox();
                    box.setType(type, strType);
                    box.setSize(size);
                    box.setLargeSize(largeSize);
                    box.setOffset(offset);
                    if (l != null) {
                        l.onAnalysis(box);
                    }
                    //mMoovBox = box;
                } else if (Box.STYPE_FREE.equals(strType)) {
                    FreeSpaceBox box = new FreeSpaceBox();
                    box.setType(type, strType);
                    box.setSize(size);
                    box.setLargeSize(largeSize);
                    box.setOffset(offset);
                    if (l != null) {
                        l.onAnalysis(box);
                    }
                    //mFreeBox = box;
                } else if (Box.STYPE_FTYH.equals(strType)) {  //ftyp
                    BYDLog.d(TAG, "analysis: is ftyp box.");
                    FileTypeBox box = new FileTypeBox();
                    box.setType(type, strType);
                    box.setSize(size);
                    box.setLargeSize(largeSize);
                    box.setOffset(offset);
                    if (l != null) {
                        l.onAnalysis(box);
                    }
                    //mFtyhBox = box;
                } else {
                    Box box = new Box();
                    box.setType(type, strType);
                    box.setSize(size);
                    box.setLargeSize(largeSize);
                    box.setOffset(offset);
                    if (l != null) {
                        l.onAnalysis(box);
                    }
                    //mBoxs.add(box);
                }
                //Read data
                //DstOffset is next box start position
                long dstOffset = 0;
                if (size > 1) {
                    dstOffset = size;
                } else if (largeSize > 0) {
                    dstOffset = largeSize;
                }
                //move the stream to position
                while (boxLen < dstOffset) {
                    if (dstOffset - boxLen > 1024) {
                        retLen = dIn.read(data);
                        if (retLen <= 0) {
                            Log.e(TAG, "analysis: read len is " + retLen);
                            return false;
                        }
                        boxLen += retLen;
                        retLen = -1;
                    } else {
                        retLen = dIn.read(data, 0, (int) (dstOffset - boxLen));
                        if (retLen <= 0) {
                            Log.e(TAG, "analysis: read len is " + retLen);
                            return false;
                        }
                        boxLen += retLen;
                        retLen = -1;
                    }
                }
                //Offset is dst offset
                offset += dstOffset;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Find MData box from stream.
     *
     * @param dIn
     * @return
     */
    public static boolean findMDatBoxOffset(DataInputStream dIn) {
        return findBoxOffsetByType(dIn, Box.STYPE_MDAT);
    }

    public static boolean findBoxOffsetByType(DataInputStream dIn, String boxType) {
        Log.d(TAG, "findBoxOffsetByType: boxType = "+boxType);
        if (dIn == null) {
            return false;
        }
        if (boxType.isEmpty()) {
            return false;
        }
        long largeSize = -1;
        int size = -1;
        byte[] type = new byte[4];
        String strType = null;
        byte[] data = new byte[1024];
        long offset = 0;
        int boxLen = 0;
        int retLen = 0;
        try {
            while (true) {
                retLen = 0;
                boxLen = 0;
                largeSize = -1;
                size = -1;
                Log.d(TAG, "findMDatBoxOffset: offset is " + offset);
                //read size
                size = dIn.readInt();   //前4个字节描述的是box整体的大小，用4个字节表示整体box的大小
                if (size <= 0) {//Err
                    Log.w(TAG, "findMDatBoxOffset: X, err and read size is " + size);
                    return false;
                }
                boxLen = 4;
                Log.d(TAG, "findMDatBoxOffset: size is " + size);
                Log.d(TAG, "findMDatBoxOffset: boxLen is " + boxLen);
                //read type
                retLen = dIn.read(type);  //type是4个字节数据，  返回值是读取到了几个字节数据
                Log.d(TAG, "findMDatBoxOffset: type is {" + (char) type[0] + "," + (char) type[1] + "," + (char) type[2] + "," + (char) type[3] + "}");
                strType = null;
                strType = "" + (char) type[0] + (char) type[1] + (char) type[2] + (char) type[3];
                Log.d(TAG, "findMDatBoxOffset: str type is " + strType);
                if (retLen != type.length) {
                    Log.w(TAG, "findMDatBoxOffset: X, err and read type len is " + retLen);
                    return false;
                }
                boxLen += retLen;
                Log.d(TAG, "findMDatBoxOffset: boxLen is " + boxLen);
                //read largeSize
                if (size == 1) {   //当size==1时，意味着Box长度需要更多bits来描述，在后面会定义一个64bits的largesize描述Box的长度（一般只有mdat类型的box才会用到large size，因为该类型要存储具体的mediaData）
                    largeSize = dIn.readLong();
                    Log.d(TAG, "findMDatBoxOffset: largeSize is " + largeSize);
                    boxLen += 8;
                    Log.d(TAG, "findMDatBoxOffset: boxLen is " + boxLen);
                }
                //init box
                if (boxType.equals(strType)) {
                    Log.w(TAG, "findMDatBoxOffset: X, find  "+boxType);
                    return true;
                }
                //Read data
                //DstOffset is next box start position
                long dstOffset = 0;
                if (size > 1) {
                    dstOffset = size;
                } else if (largeSize > 0) {
                    dstOffset = largeSize;
                }
                Log.d(TAG, "findMDatBoxOffset: dstOffset is " + dstOffset);
                Log.d(TAG, "findMDatBoxOffset: boxLen is " + boxLen);

                if(boxType.equals(Box.STYPE_UUID) && strType.equals(Box.STYPE_MDAT)){
                    retLen = dIn.skipBytes((int)dstOffset);
                    Log.d(TAG, "findBoxOffsetByType: retLen = "+retLen);
                }else {
                    //move the stream to position
                    while (boxLen < dstOffset) {
                        if (dstOffset - boxLen > 1024) {
                            retLen = dIn.read(data);
                            if (retLen <= 0) {
                                Log.w(TAG, "analysis: X, err and read len is " + retLen);
                                return false;
                            }
                            boxLen += retLen;
                            retLen = -1;
                        } else {
                            retLen = dIn.read(data, 0, (int) (dstOffset - boxLen));
                            if (retLen <= 0) {
                                Log.w(TAG, "analysis: X, err and read len is " + retLen);
                                return false;
                            }
                            boxLen += retLen;
                            retLen = -1;
                        }
                    }
                    //Offset is dst offset
                    offset += dstOffset;

                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.w(TAG, "analysis: X, err and not found mdat box");
        return false;
    }


    public static boolean writeDataToMp4(String filePath, String point) {
        if (!new File(filePath).exists()) {
            return false;
        }
        byte[] type = Box.BTYPE_UUID;
        int size = 4 + type.length + Box.USERTYPE.length() + 4 + point.length() + 4;
        Log.d(TAG, "writeMDatBoxOffset: type is {" + (char) type[0] + "," + (char) type[1] + "," + (char) type[2] + "," + (char) type[3] + "}");
        Log.d(TAG, "writeMDatBoxOffset: point.length() is " + point.length());
        RandomAccessFile randomFile = null;
        try {
            // 打开一个随机访问文件流，按读写方式
            randomFile = new RandomAccessFile(filePath, "rw");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            // 将写文件指针移到文件尾。
            randomFile.seek(fileLength);
            randomFile.writeInt(size);
            randomFile.write(type);
            randomFile.writeBytes(Box.USERTYPE);
            randomFile.writeInt(point.length());
            randomFile.write(point.getBytes());
            randomFile.writeInt(0);
            BYDLog.d(TAG, "writeMDatBoxOffset: success");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            BYDLog.d(TAG, "writeMDatBoxOffset: finally");
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static String readeDatafromMp4(String filePath) {
        DataInputStream dIn = null;
        try {
            dIn = new DataInputStream(new FileInputStream(filePath));
            int retLen = 0;
            boolean find = findBoxOffsetByType(dIn, Box.STYPE_UUID);
            if (find) {
                byte[] uType = new byte[Box.USERTYPE.length()];
                int len = dIn.read(uType);
                String userType = new String(uType);
                Log.d(TAG, "readeDatafromMp4:  len = " + len + "   " + userType);
                if (userType.equals(Box.USERTYPE)) {
                    int dataLen = dIn.readInt();
                    byte[] data = new byte[dataLen];
                    retLen = dIn.read(data);
                    return new String(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(dIn != null){
                try {
                    dIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dIn = null;
            }
        }
        return null;
    }
}
