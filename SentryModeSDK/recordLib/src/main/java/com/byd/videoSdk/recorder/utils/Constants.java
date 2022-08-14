package com.byd.videoSdk.recorder.utils;

public class Constants {

    public final static int IMAGE_WIDTH_1280 = 1280;
    public final static int IMAGE_HEIGHT_720 = 720;
    public final static int IMAGE_HEIGHT_1920 = 1920;
    public final static int IMAGE_HEIGHT_960 = 960;
    public final static int IMAGE_HEIGHT_1080 = 1080;

    public final static int IMAGE_WIDTH_2560 = 2560;
    public final static int IMAGE_HEIGHT_1440 = 1440;

//    public static int IMAGE_WIDTH = BydCameraObject.isPanoHCamera() ? IMAGE_WIDTH_2560 : IMAGE_WIDTH_1280;//2560;1280
//    public static int IMAGE_HEIGHT = BydCameraObject.isPanoHCamera() ? IMAGE_HEIGHT_1440 : IMAGE_HEIGHT_960;///1440;960

    public static int IMAGE_WIDTH = IMAGE_WIDTH_2560;
    public static int IMAGE_HEIGHT = IMAGE_HEIGHT_1920;

    public static int IMAGE_BUFFER_SIZE = IMAGE_WIDTH * IMAGE_HEIGHT * 3 / 2;

    private static final int COMPRESS_RATIO = 256;  //压缩比
    public static int FRAME_RATE = 25;      //帧率
    public static int BIT_RATE =  (IMAGE_WIDTH * IMAGE_HEIGHT / COMPRESS_RATIO) * 3 * 8 * FRAME_RATE;        //比特率，码率 编码器每秒编出的数据大小
    public static int I_FRAME_INTERVAL = 1; //I帧将要出现的时间间隔


    public static int MODE_BUF = 0x01;
    public static int MODE_CODEC = 0x02;
    public static int RECORD_MODE = MODE_CODEC;//MODE_BUF;


    public static void setRecordConfig(RecordConfig recordConfig) {
        if (recordConfig != null) {
            IMAGE_WIDTH = recordConfig.getWIDTH();
            IMAGE_HEIGHT = recordConfig.getHEIGHT();
            FRAME_RATE = recordConfig.getFRAME_RATE();
            BIT_RATE = recordConfig.getBIT_RATE();
            I_FRAME_INTERVAL = recordConfig.getI_FRAME_INTERVAL();
            BIT_RATE =  (IMAGE_WIDTH * IMAGE_HEIGHT / COMPRESS_RATIO) * 3 * 8 * FRAME_RATE;
        }
    }
}
