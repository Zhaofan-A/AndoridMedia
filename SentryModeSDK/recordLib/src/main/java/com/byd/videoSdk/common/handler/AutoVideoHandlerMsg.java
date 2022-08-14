package com.byd.videoSdk.common.handler;

public class AutoVideoHandlerMsg {

    public static final int MSG_SHIFT_BIT = 5;  // left shift or right shift

    // MSG MODE
    public static final int MSG_MODE_REMOVE_SEND = 0; // remove the message and then send it
    public static final int MSG_MODE_CONTAIN_SEND = 1; // if contain the message then not send
    public static final int MSG_MODE_DIRECTLY_SEND = 2; // whether the message is contoin or not sent directly


    // DVR MSG TYPE
    public static final int SENTRYMODE_MSG_TYPE_TFCARD = 0x000100; // TF CARD MESSAGE(SCAN FILE)
    public static final int SENTRYMODE_MSG_TYPE_UI = 0x000200; // UPDATE UI MESSAGE
    public static final int SENTRYMODE_MSG_TYPE_MEDIA = 0x000400; // PHOTO MESSAGE and VIDEO MESSAGE(PLAY,PAUSE,SEEK,STOP)
    public static final int SENTRYMODE_MSG_TYPE_RECORD = 0x000800; // RECODE MESSAGE(NORMAL,EVENT)
    public static final int SENTRYMODE_MSG_TYPE_ALBUM = 0x001000; // ALBUM MESSAGE
    public static final int SENTRYMODE_MSG_TYPE_MODEL = 0x002000; // UPDATE MODEL
    public static final int SENTRYMODE_MSG_TYPE_TF_OBSERVE = 0X004000; // observe tf card DIR
    public static final int SENTRYMODE_MSG_TYPE_RECOVER = 0X008000; // video recover
    public static final int SENTRYMODE_MSG_TYPE_SDCARD_PARTITION_MONITOR = 0x00010000; // monitor sd card partition


    public static final int MSG_TYPE_ALL = 0X0080000000 - 1; // ALL MESSAGE
}
