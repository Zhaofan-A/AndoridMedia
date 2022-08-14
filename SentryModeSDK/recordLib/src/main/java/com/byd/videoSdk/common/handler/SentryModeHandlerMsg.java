package com.byd.videoSdk.common.handler;

public class SentryModeHandlerMsg {

    public static final int MSG_SHIFT_BIT = AutoVideoHandlerMsg.MSG_SHIFT_BIT;  // left shift or right shift

    // SENTRYMODE MSG TYPE
    public static final int SENTRYMODE_MSG_TYPE_TFCARD = AutoVideoHandlerMsg.SENTRYMODE_MSG_TYPE_TFCARD; // TF CARD MESSAGE(SCAN FILE)
    public static final int SENTRYMODE_MSG_TYPE_UI = AutoVideoHandlerMsg.SENTRYMODE_MSG_TYPE_UI; // UPDATE UI MESSAGE
    public static final int SENTRYMODE_MSG_TYPE_MEDIA = AutoVideoHandlerMsg.SENTRYMODE_MSG_TYPE_MEDIA; // PHOTO MESSAGE and VIDEO MESSAGE(PLAY,PAUSE,SEEK,STOP)
    public static final int SENTRYMODE_MSG_TYPE_RECORD = AutoVideoHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD; // RECODE MESSAGE(NORMAL,EVENT)
    public static final int SENTRYMODE_MSG_TYPE_MODEL = AutoVideoHandlerMsg.SENTRYMODE_MSG_TYPE_MODEL; // UPDATE MODEL
    public static final int SENTRYMODE_MSG_TYPE_TF_OBSERVE = AutoVideoHandlerMsg.SENTRYMODE_MSG_TYPE_TF_OBSERVE; // observe tf card DIR
    public static final int SENTRYMODE_MSG_TYPE_RECOVER = AutoVideoHandlerMsg.SENTRYMODE_MSG_TYPE_RECOVER; // video check recover end
    public static final int SENTRYMODE_MSG_TYPE_ALL = AutoVideoHandlerMsg.MSG_TYPE_ALL;

    // TF CARD MESSAGE
    public static final int SENTRYMODE_MSG_TYPE_TFCARD_SCAN = (SENTRYMODE_MSG_TYPE_TFCARD << MSG_SHIFT_BIT) + 1;
    public static final int SENTRYMODE_MSG_TYPE_TFCARD_RESUME_FILE = (SENTRYMODE_MSG_TYPE_TFCARD << MSG_SHIFT_BIT) + 2;

    // RECORD MESSAGE
    public static final int SENTRYMODE_MSG_TYPE_RECORD_VIDEO_START = (SENTRYMODE_MSG_TYPE_RECORD << MSG_SHIFT_BIT) + 1;
    public static final int SENTRYMODE_MSG_TYPE_RECORD_VIDEO_END = (SENTRYMODE_MSG_TYPE_RECORD << MSG_SHIFT_BIT) + 2;
    public static final int SENTRYMODE_MSG_TYPE_RECORD_VIDEO_SYNC_END = (SENTRYMODE_MSG_TYPE_RECORD << MSG_SHIFT_BIT) + 3;

    //RECOVER MESSAGE
    public static final int SENTRYMODE_MSG_TYPE_RECOVER_VIDEO_START = (SENTRYMODE_MSG_TYPE_RECOVER << MSG_SHIFT_BIT) + 1;
    public static final int SENTRYMODE_MSG_TYPE_RECOVER_VIDEO_END = (SENTRYMODE_MSG_TYPE_RECOVER << MSG_SHIFT_BIT) + 2;

    //TF OBSERVE MESSAGE
    public static final int SENTRYMODE_MSG_TYPE_TF_OBSERVE_DIR_CREATED = (SENTRYMODE_MSG_TYPE_TF_OBSERVE << MSG_SHIFT_BIT) + 1;



   public static String getMessageName(int msg){
       String result = "no current msg";
       switch (msg){
           case SENTRYMODE_MSG_TYPE_TFCARD_SCAN:
               result = "SENTRYMODE_MSG_TYPE_TFCARD_SCAN";
               break;
           case SENTRYMODE_MSG_TYPE_TFCARD_RESUME_FILE:
               result = "SENTRYMODE_MSG_TYPE_TFCARD_RESUME_FILE";
               break;
           case SENTRYMODE_MSG_TYPE_RECORD_VIDEO_START:
               result = "SENTRYMODE_MSG_TYPE_RECORD_VIDEO_START";
               break;
           case SENTRYMODE_MSG_TYPE_RECORD_VIDEO_END:
               result = "SENTRYMODE_MSG_TYPE_RECORD_VIDEO_END";
               break;
           case SENTRYMODE_MSG_TYPE_RECORD_VIDEO_SYNC_END:
               result = "SENTRYMODE_MSG_TYPE_RECORD_VIDEO_SYNC_END";
               break;
           case SENTRYMODE_MSG_TYPE_TF_OBSERVE_DIR_CREATED:
               result = "SENTRYMODE_MSG_TYPE_TF_OBSERVE_DIR_CREATED";
               break;
           case SENTRYMODE_MSG_TYPE_RECOVER_VIDEO_START:
               result = "SENTRYMODE_MSG_TYPE_RECOVER_VIDEO_START";
               break;
           case SENTRYMODE_MSG_TYPE_RECOVER_VIDEO_END:
               result = "SENTRYMODE_MSG_TYPE_RECOVER_VIDEO_END";
               break;
           default:break;
       }
       return result;
   }


}
