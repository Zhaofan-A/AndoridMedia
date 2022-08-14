package com.byd.videoSdk.recorder.utils;

import com.byd.videoSdk.common.handler.AutoVideoHandler;
import com.byd.videoSdk.common.handler.SentryModeHandlerMsg;
import com.byd.videoSdk.common.util.BydFileUtils;
import com.byd.videoSdk.common.util.SharedPreferencesUtils;
import com.byd.videoSdk.recorder.RecorderController;
import com.byd.videoSdk.common.util.BYDLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FileSwapHelper {
    private static final String TAG = "FileSwapHelper";

    private static String ROOT_PATH = null;

    public static final String LOCK = "EVT";//普通紧急视频
    public static final String NORMAL = "NOR";//循环视频
    public static final String COPY = "COPY";//备份视频临时路径
    public static final String ROOT = "ROOT";//哨兵根目录
    public static final String PHOTO = "PHOTO";//哨兵拍照目录

    public static String BASE_EXT = ".mp4";
    private static final String SIMPLE_DATE_FORMAT = "yyyyMMdd_HHmmss";
    public static String MP4_CPY_EXT = ".mp4_cpy";
    public static String MP4_INDEX_EXT = ".mp4_index";
    public static String VIDEO_EXT = "_M_00001";
    public static String COPY_EXT = ".h264";
    public static String FLV_EXT = ".flv";

    public static String SENTRYMODE_SDK_RECORDER_INFO = "sentrymode_sdk_recorder_info";
    public static String CUSTOM_VIDEO_SAVE_PATH = "custom_video_save_path";

    public static String getNextFileName(String mPrefixName) {
        String fileName = getFileName(mPrefixName);
        String nextFileName = getSaveFilePath(fileName);
        BYDLog.d(TAG, "nextFileName = " + nextFileName);
        return nextFileName;
    }

    private static String getFileName(String prefix) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
        String fileName = "";
        fileName = prefix + "_" + simpleDateFormat.format(new Date()) + VIDEO_EXT;
        BYDLog.d(TAG, "get cloud time as file name = " + fileName);
        return fileName;
    }

    private static String getSaveFilePath(String fileName) {
        StringBuilder fullPath = new StringBuilder();

        fullPath.append(getVideoPath(NORMAL));
        fullPath.append(fileName);
        fullPath.append(BASE_EXT);

        String string = fullPath.toString();
        BYDLog.d(TAG, "fullPath = " + string);
        File file = new File(string);
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                BYDLog.d(TAG, "mkdirs fail");
                return null;
            } else {
                BYDLog.d(TAG, "mkdirs success");
            }
        }
        AutoVideoHandler.getInstance().sendMessage(SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_TF_OBSERVE_DIR_CREATED, parentFile.getAbsolutePath());
        return string;
    }

    public static String getVideoPath(String type) {
        BYDLog.d(TAG, "getVideoPath: type = " + type);
        String videoPath = ROOT_PATH;
        if (videoPath == null) {
            videoPath = BydFileUtils.getFilesPath(RecorderController.getInstance().getContext()) + "/SentryModeSDK/";
        }
        if(type == null){
            return videoPath;
        }
        switch (type) {
            case NORMAL:
                videoPath = videoPath + NORMAL + File.separator;
                break;
            case LOCK:
                videoPath = videoPath + LOCK + File.separator;
                break;
            case COPY:  //用于视频恢复得索引文件放到统一指定得地方
                videoPath = BydFileUtils.getFilesPath(RecorderController.getInstance().getContext()) + File.separator+ COPY + File.separator;
                break;
            case PHOTO:
                videoPath = videoPath + PHOTO + File.separator;
                break;
            default:
                break;
        }
        BYDLog.d(TAG, "getVideoPath: " + videoPath);
        return videoPath;
    }

    public static String getVideoIndexPath(String videoAbsPath) {
        String fileName = new File(videoAbsPath).getName();
        BYDLog.d(TAG, "getVideoIndexPath: " + fileName);
        fileName = fileName.replace(FileSwapHelper.BASE_EXT, FileSwapHelper.MP4_INDEX_EXT);
        fileName = getVideoPath(FileSwapHelper.COPY) + fileName;
        BYDLog.d(TAG, "getVideoIndexPath: " + fileName);
        return fileName;
    }


    /**
     * 设置自定义存储路径
     *
     * @param path
     */
    public static void setRootPath(String path) {
        ROOT_PATH = path + File.separator;
        //保存是为了视频恢复时使用
        SharedPreferencesUtils.getInstance(RecorderController.getInstance().getContext(), SENTRYMODE_SDK_RECORDER_INFO).putString(CUSTOM_VIDEO_SAVE_PATH, ROOT_PATH);
        BYDLog.d(TAG, "setRootPath: ROOT_PATH = " + ROOT_PATH);
    }

}
