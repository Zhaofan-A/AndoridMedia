package com.byd.videoSdk.recover;


import com.byd.videoSdk.common.util.BYDLog;

import java.util.concurrent.TimeUnit;

/**
 * Backup constants interface.
 *
 * @author bai.yu1
 */
public class VideoBackupConstants {
    private static final String TAG = "VideoBackupConstants";

    public static final int CDR_BACKUP_TYPE_FLV = 1000;
    public static final int CDR_BACKUP_TYPE_H264FRAME = 1001;
    public static final int CDR_BACKUP_TYPE_MDATINDEX = 1002;

    public static final String  EVENT_RECORDER_BACKUP = "event_recorder_backup";

    private static long mSDCardMountTime = 0; // cdr start running or sd card mount time
    private static long MOUNTING_TIME = 2 * 60 * 1000; // cdr running or sd card mount MOUNTING_TIME, start sync backup file
    private static boolean mEnableCopy;


    /**
     * Current type.
     * Is flv or '.h264 frame' file.
     *
     * @return
     */
    public static int getBackupType() {
        //return CDR_BACKUP_TYPE_FLV;
//        return CDR_BACKUP_TYPE_H264FRAME;
        return CDR_BACKUP_TYPE_MDATINDEX;
    }

    /**
     * record cdr start time or sd card mount time
     *
     * @return
     */
    public static void setSDCardMountTime() {
        BYDLog.d(TAG, "setSDCardMountTime");
        mSDCardMountTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    /**
     * Is start sync backup file
     *
     * @return
     */
    public static boolean isSyncBackupFile() {
        if (TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) > (mSDCardMountTime + MOUNTING_TIME)) {
            return true;
        }
        return false;
    }

    /**
     * is enable copy
     *
     * @return
     */
    public static boolean isEnableCopy() {
        return mEnableCopy;
    }

    /**
     *
     * @param enableCopy
     */
    public static void setEnableCopy(boolean enableCopy) {
        mEnableCopy = enableCopy;
    }



}
