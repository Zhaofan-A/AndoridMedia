package com.byd.videoSdk.recover.mdat.box;

import android.util.Log;

public class Box {
	public static final String TAG = "Box";
	//Java int is 4byte
	//The type ascii is "ftyh"
	public static final String STYPE_FTYH = "ftyp";
	public static final byte[] BTYPE_FTYH = {0x66, 0x74, 0x79, 0x70};
	public static final int ITYPE_FTYH = 0x66747970;
	//The type ascii is "free"
	public static final String STYPE_FREE = "free";
	public static final byte[] BTYPE_FREE = {0x66, 0x72, 0x65, 0x65};
	public static final int ITYPE_FREE = 0x66726565;
	//The type ascii is "moov"
	public static final String STYPE_MOOV = "moov";
	public static final byte[] BTYPE_MOOV = {0x6D, 0x6F, 0x6F, 0x76};
	public static final int ITYPE_MOOV = 0x6D6F6F76;
	//The type ascii is "mdat"
	public static final String STYPE_MDAT = "mdat";
	public static final byte[] BTYPE_MDAT = {0x6D, 0x64, 0x61, 0x74};
	public static final int ITYPE_MDAT = 0x6D646174;
	//The type ascii is "uuid"
	public static final String STYPE_UUID = "uuid";
	public static final byte[] BTYPE_UUID = {0x75, 0x75, 0x69, 0x64};
	public static final int ITYPE_UUID = 0x75756964;

	// uuid box exits usertype
	public static final String USERTYPE ="trigger_point";

	//The type ascii is "mdat"
//	public static final String STYPE_MDAT = "uuid";
//	public static final byte[] BTYPE_MDAT = {0x6D, 0x64, 0x61, 0x64};
//	public static final int ITYPE_MDAT = 0x6D646174;

	private int mSize = -1;
	private int mType = -1;
	private String mStrType = null;
	private byte[] mByteType = null;
	private long mLargeSize = -1;
	private long mOffset = -1;

	public Box(){}
	public void setSize(int size){
		mSize = size;
	}
	public int getSize(){
		return mSize;
	}
	public void setLargeSize(long largeSize){
		mLargeSize = largeSize;
	}
	public long getLargeSize(){
		return mLargeSize;
	}
	public void setOffset(long offset){
		mOffset = offset;
	}
	public long getOffset(){
		return mOffset;
	}
	public int getType(){
		return mType;
	}
	public String getStrType(){
		return mStrType;
	}
	public void setType(byte[] type, String sType){
		if(STYPE_FTYH.equals(sType)){
			mByteType = BTYPE_FTYH;
		    mType = ITYPE_FTYH;
			mStrType = STYPE_FTYH;
		} else if(STYPE_FREE.equals(sType)){
			mType = ITYPE_FREE;
			mByteType = BTYPE_FREE;
			mStrType = STYPE_FREE;
		} else if(STYPE_MOOV.equals(sType)){
			mType = ITYPE_MOOV;
			mByteType = BTYPE_MOOV;
			mStrType = STYPE_MOOV;
		} else if(STYPE_MDAT.equals(sType)){
			mType = ITYPE_MDAT;
			mByteType = BTYPE_MDAT;
			mStrType = STYPE_MDAT;
		}
	}
	public void setType(int type){
		mType = type;
		switch(type){
		case ITYPE_FTYH:
			mStrType = STYPE_FTYH;
			break;
		case ITYPE_FREE:
			mStrType = STYPE_FREE;
			break;
		case ITYPE_MOOV:
			mStrType = STYPE_MOOV;
			break;
		case ITYPE_MDAT:
			mStrType = STYPE_MDAT;
			break;
		}
	}
	public void dump(){
		Log.i(TAG, "dump: type code is "+ Integer.toHexString(mType) + " and type name is " + mStrType);
		Log.i(TAG, "dump: size is "+ mSize + " and largeSize is " + mLargeSize);
		Log.i(TAG, "dump: offset is "+ mOffset);
	}
}
