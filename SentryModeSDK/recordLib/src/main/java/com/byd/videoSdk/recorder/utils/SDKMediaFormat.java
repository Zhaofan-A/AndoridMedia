package com.byd.videoSdk.recorder.utils;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;


import com.byd.videoSdk.recorder.RecorderController;
import com.byd.videoSdk.common.util.BYDLog;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The default mpeg4 media format for media codec and media muxer.
 *
 * @author bai.yu1
 */
public class SDKMediaFormat {
    private static final String TAG = "SDKMediaFormat";

    public static String KEY_PPS = "pps";
    public static String KEY_SPS = "sps";

    private static final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;//"video/avc"; // H.264 Advanced Video
    private static Mpeg4_1080P mMpeg4_1080P = null;

    /**
     * Create the mpeg4 media format
     *
     * @return
     */
    public static MediaFormat getMPEG4MediaFormat() {
        //  mMediaCodec.configure   编码时用的是这个，不需要sps和pps
        if (mMpeg4_1080P == null) {
            mMpeg4_1080P = new Mpeg4_1080P();
        }
        // no take sps pps
        return mMpeg4_1080P.getMediaFormat(false);
    }


    /**
     * Create the mpeg4 media format
     *
     * @return
     */
    public static MediaFormat getMPEG4MediaFormat(byte[] sps, byte[] pps) {
        //mdat视频恢复方式走的是这个
        if (mMpeg4_1080P == null) {
            mMpeg4_1080P = new Mpeg4_1080P();
        }
        mMpeg4_1080P.setSps(sps);
        mMpeg4_1080P.setPps(pps);
        return mMpeg4_1080P.getMediaFormat(true);
    }

    /**
     * Create the mpeg4 media format
     *
     * @return
     */
    public static MediaFormat getMediaFormatBySpsPps() {
        //mdat视频恢复方式走的是这个
        if (mMpeg4_1080P == null) {
            mMpeg4_1080P = new Mpeg4_1080P();
        }
        byte[] pps = getVideoSpsOrPps(SDKMediaFormat.KEY_PPS);
        byte[] sps = getVideoSpsOrPps(SDKMediaFormat.KEY_SPS);

        if (pps == null || sps == null) {
            return null;
        }
        BYDLog.d(TAG, "getMediaFormatBySpsPps: " + Arrays.toString(pps));
        BYDLog.d(TAG, "getMediaFormatBySpsPps: " + Arrays.toString(sps));

        mMpeg4_1080P.setSps(sps);
        mMpeg4_1080P.setPps(pps);
        return mMpeg4_1080P.getMediaFormat(true);
    }


    public static void printByteBuffer(MediaFormat f, String key) {
        if (f == null || key == null) {
            BYDLog.e(TAG, "printByteBuffer: input is null");
            return;
        }
        ByteBuffer bf = f.getByteBuffer(key);
        if (bf == null) {
            BYDLog.w(TAG, "encodeFrame: key[" + key + "] byte buffer is null");
            return;
        }
        byte[] byteArray = bf.array();
        if (byteArray != null && byteArray.length > 0) {
            for (int i = 0; i < byteArray.length; i++) {
                BYDLog.d(TAG, "printByteBuffer:  th[" + i + "] is [" + byteArray[i] + "]");
            }
        } else {
            BYDLog.w(TAG, "encodeFrame: key[" + key + "] is null");
        }
    }

    /**
     * 获取最后一次编码器输出得mediaformat中得pps和sps
     *
     * @param type
     */
    public static byte[] getVideoSpsOrPps(String type) {
        String result;
        if (SDKMediaFormat.KEY_PPS.equals(type)) {
            result = SharedPreferencesManager.getInstance(RecorderController.getInstance().getContext()).getString(SDKMediaFormat.KEY_PPS, null);
        } else {
            result = SharedPreferencesManager.getInstance(RecorderController.getInstance().getContext()).getString(SDKMediaFormat.KEY_SPS, null);
        }
        if (result != null) {
            return result.getBytes();
        }
        return null;
    }

    /**
     * 保存最后一次编码器输出得mediaformat中得pps和sps
     *
     * @param type
     * @param type
     */
    public static void setVideoSpsOrPps(String type, byte[] bytes) {
        if (SDKMediaFormat.KEY_PPS.equals(type)) {
            SharedPreferencesManager.getInstance(RecorderController.getInstance().getContext()).putString(SDKMediaFormat.KEY_PPS, new String(bytes));
        } else {
            SharedPreferencesManager.getInstance(RecorderController.getInstance().getContext()).putString(SDKMediaFormat.KEY_SPS, new String(bytes));
        }
    }

    static class Mpeg4_1080P {
        /**
         * Media Format defined.
         */
        private int mColorFormat;
        private MediaCodecInfo codecInfo;
        private MediaFormat mMediaFormat;
        private byte[] mSps;
        private byte[] mPps;


        /**
         * Get sps buffer.
         *
         * @return
         */
        public void setSps(byte[] sps) {
            mSps = sps;
        }

        /**
         * Get pps buffer.
         *
         * @return
         */
        public void setPps(byte[] pps) {
            mPps = pps;
        }

        /**
         * Get the mediaformat that`s attrabute is for mpeg4.
         *
         * @return
         */
        public MediaFormat getMediaFormat(boolean takeCsd) {
            int w, h;
            if (Constants.RECORD_MODE == Constants.MODE_BUF) {
                w = Constants.IMAGE_WIDTH_1280;
                h = Constants.IMAGE_HEIGHT_960;
            } else {
                w = Constants.IMAGE_WIDTH;
                h = Constants.IMAGE_HEIGHT;
            }
            return init(w, h, takeCsd);
        }


        //TODO  后续有时间把byte编码方式也加上
        boolean isOpengl = true;

        /**
         * ADD VIDEO FORMAT:
         * {
         * csd-1=java.nio.HeapByteBuffer[pos=0 lim=8 cap=8],
         * mime=video/avc,
         * frame-rate=25,
         * width=1920,
         * height=1080,
         * bitrate=4860000,
         * csd-0=java.nio.HeapByteBuffer[pos=0 lim=22 cap=22],
         * max-bitrate=4860000
         * }
         */
        private MediaFormat init(int w, int h, boolean takeCsd) {
            codecInfo = selectCodec(MIME_TYPE);
            if (codecInfo == null) {
                BYDLog.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
                return null;
            }

            if (isOpengl) {
                mColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
            } else {
                mColorFormat = selectColorFormat(codecInfo, MIME_TYPE);
            }

            mMediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGHT);
            BYDLog.d(TAG, "init: createVideoFormat w = " + w + " h = " + h);
            if (takeCsd && mPps != null && mSps != null) {
                mMediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(mPps));
                mMediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(mSps));
            }

            mMediaFormat.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_AVC);
            mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, Constants.FRAME_RATE);
            mMediaFormat.setInteger(MediaFormat.KEY_WIDTH, Constants.IMAGE_WIDTH);
            mMediaFormat.setInteger(MediaFormat.KEY_HEIGHT, Constants.IMAGE_HEIGHT);
            mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, Constants.BIT_RATE);  //编码器每秒编出的数据大小
            mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);
            mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Constants.I_FRAME_INTERVAL);
            BYDLog.d(TAG, "init: format is " + mMediaFormat.toString());
            return mMediaFormat;
        }

        private int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
            MediaCodecInfo.CodecCapabilities capabilities = codecInfo
                    .getCapabilitiesForType(mimeType);
            for (int i = 0; i < capabilities.colorFormats.length; i++) {
                int colorFormat = capabilities.colorFormats[i];
                if (isRecognizedFormat(colorFormat)) {
                    return colorFormat;
                }
            }
            return 0; // not reached
        }

        public static MediaCodecInfo selectCodec(String mimeType) {
            int numCodecs = MediaCodecList.getCodecCount();
            for (int i = 0; i < numCodecs; i++) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
                if (!codecInfo.isEncoder()) {
                    continue;
                }
                String[] types = codecInfo.getSupportedTypes();
                for (int j = 0; j < types.length; j++) {
                    if (types[j].equalsIgnoreCase(mimeType)) {
                        return codecInfo;
                    }
                }
            }
            return null;
        }

        /**
         * 是否是正确得format
         * @param colorFormat
         * @return
         */
        private boolean isRecognizedFormat(int colorFormat) {
            switch (colorFormat) {
                // these are the formats we know how to handle for this test
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
                case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                    return true;
                default:
                    return false;
            }
        }

    }
}
