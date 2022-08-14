package com.byd.videoSdk.recover;

import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;


import com.byd.videoSdk.common.handler.AutoVideoHandler;
import com.byd.videoSdk.common.handler.AutoVideoHandlerListener;
import com.byd.videoSdk.common.handler.SentryModeHandler;
import com.byd.videoSdk.common.handler.SentryModeHandlerMsg;
import com.byd.videoSdk.recorder.file.VideoMpeg4File;
import com.byd.videoSdk.sdcard.SDCardStatus;
import com.byd.videoSdk.common.util.BYDLog;

import java.io.File;
import java.util.ArrayList;

/**
 * For resume the h264 file to mpeg4.
 *
 * @author bai.yu1
 */
public abstract class Mp4ResumeController {
    private static final String TAG = "Mp4ResumeController";

    private static Mp4ResumeController mInstance = null;
    private HandlerThread myHandlerThread;
    private static Handler mHandler;

    private static String mCurrentCopyFile = null;//Current recorder meg4 copy file.
    //    private static boolean mResumeFile = true; // is resume from copy to normal
    private static AutoVideoHandlerListener mDVRListner = new AutoVideoHandlerListener() {

        @Override
        public void onNotify(int msg, int ext1, int ext2, Object obj) {
            // TODO Auto-generated method stub
            BYDLog.d(TAG, "onNotify: msg is " + SentryModeHandlerMsg.getMessageName(msg));
            BYDLog.d(TAG, "onNotify: obj is " + obj);
            if (SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD_VIDEO_START == msg) {
                if (obj != null && obj instanceof String) {
                    Mp4ResumeController.getInstance().setCurrentFile((String) obj);
                }
            } else if (SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD_VIDEO_SYNC_END == msg) {

                BYDLog.d(TAG, "onNotify: mInstance " + mInstance);
                BYDLog.d(TAG, "onNotify: mHandler " + mHandler);
                if (mInstance != null && mHandler != null) {
                    Message msgCopy = new Message();
                    msgCopy.what = MSG_RECORDER_NORMAL_STOP;
                    msgCopy.arg1 = ext1;
                    msgCopy.arg2 = ext2;
                    msgCopy.obj = obj;
                    mHandler.sendMessageDelayed(msgCopy, MSG_RECORDER_NORMAL_STOP_DELAYED);

                    /*
                     * Rm current file name, if not new file recording.
                     * If has new file recording , current file can be not the stop file.
                     */
                    if (obj != null && obj instanceof String) {
                        String name = (String) obj;
                        String curName = Mp4ResumeController.getInstance().getCurrentFile();
                        BYDLog.d(TAG, "onNotify: name is " + name);
                        BYDLog.d(TAG, "onNotify: curName is " + curName);
                        if (curName != null && (curName.equals(name))) {
                            Mp4ResumeController.getInstance().setCurrentFile(null);
                        }
                    }
                }
            }
        }

        @Override
        public boolean isCallback(int msg, int type) {
            return SentryModeHandler.isCallback(msg, type);
        }

    };

    private static final int MSG_CHECK = 1000;
    private static final int MSG_RECORDER_NORMAL_STOP = 1001;
    private static final int MSG_RECORDER_NORMAL_STOP_DELAYED = 0;//30000;
    protected static final int DELETE_COPY_DELAY_TIME = 30 * 1000;
    private static final int MSG_START_CHECK_SDCARD_DELAY = 5 * 1000;

    protected Mp4ResumeController() {
        SDCardStatus.getInstance().registerSDStateChangedListener(new SDCardStatus.SDCardStatusListener() {
            @Override
            public void onSDCardStateChanged(int state) {
                BYDLog.d(TAG, "onSDCardStateChanged state " + state);
                Mp4ResumeController.getInstance().setCurrentFile(null);
                if (state == SDCardStatus.SDCARD_EJECT) {
                    Mp4ResumeController.getInstance().removeRecorderNoramlStopMsg();
                } else if (state == SDCardStatus.SDCARD_MOUNTED) {
                    VideoBackupConstants.setSDCardMountTime();
                }
            }
        });

        if (mHandler == null) {
            //创建一个线程,线程名字：handler-thread
            myHandlerThread = new HandlerThread("Mp4ResumeController-thread");
            //开启一个线程
            myHandlerThread.start();
            //在这个线程中创建一个handler对象
            mHandler = new Handler(myHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    //这个方法是运行在 handler-thread 线程中的 ，可以执行耗时操作
                    BYDLog.d(TAG, "msg: " + msg.what + "  thread: " + Thread.currentThread().getName());
                    process(msg);
                }
            };
            AutoVideoHandler.getInstance().registerHandlerListener(mDVRListner, SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECORD);
        }
    }

    /**
     * Get the instance.
     *
     * @return
     */
    public synchronized static Mp4ResumeController getInstance() {
        if (mInstance == null) {
            int type = VideoBackupConstants.getBackupType();
            if (VideoBackupConstants.CDR_BACKUP_TYPE_MDATINDEX == type) {
                mInstance = new MP4IndexResumeCtrlImpl();
            } else {
                BYDLog.e(TAG, "getInstance: backuo type[" + type + "] is err");
            }
        }
        return mInstance;
    }

    /**
     * Start check the copy file and copy file to mpeg4 file.
     */
    public synchronized void startCheck() {
        BYDLog.d(TAG, "startCheck");
        if (mInstance != null && mHandler != null) {
            mHandler.removeMessages(MSG_CHECK);
            mHandler.sendEmptyMessageDelayed(MSG_CHECK, MSG_START_CHECK_SDCARD_DELAY);
        }
    }

    public synchronized void stopCheck() {
        BYDLog.d(TAG, "stopCheck");
        if (mInstance != null && mHandler != null) {
            mHandler.removeMessages(MSG_CHECK);
            mHandler.removeMessages(MSG_RECORDER_NORMAL_STOP);
        }
        stopResume();
    }

    /**
     * getCurrentFile.
     *
     * @return
     */
    protected synchronized String getCurrentFile() {
        BYDLog.d(TAG, "getCurrentFile: " + mCurrentCopyFile);
        return mCurrentCopyFile;
    }

    /**
     * setCurrentFile
     *
     * @param name
     */
    protected synchronized void setCurrentFile(String name) {
        BYDLog.d(TAG, "setCurrentFile: " + name);
        mCurrentCopyFile = name;
    }

    /**
     * copyFileExist
     *
     * @param name
     * @return
     */
    public boolean copyFileExist(String name) {
        return false;
    }

    /**
     * Post runnable
     *
     * @param r
     * @param delayMS
     */
    protected synchronized void postRunnable(Runnable r, long delayMS) {
        if (mInstance != null && mInstance.mHandler != null) {
            mInstance.mHandler.removeCallbacks(r);
            if (delayMS > 0) {
                mInstance.mHandler.postDelayed(r, delayMS);
            } else {
                mInstance.mHandler.post(r);
            }
        }
    }

    protected abstract boolean stopResume();

    /**
     * process resume.
     *
     * @return
     */
    protected abstract ArrayList<String> processResume();

    /**
     * ProcessStop.
     *
     * @return
     */
    protected abstract String processStop(String name);

    /**
     * Process the message.
     *
     * @param msg
     */
    private void process(Message msg) {
        if (msg == null) {
            BYDLog.w(TAG, "process: msg is null");
            return;
        }
        BYDLog.d(TAG, "process: msg is " + msg.what);
        switch (msg.what) {
            case MSG_CHECK:
                //Sub class implements.
                BYDLog.d(TAG, "process");
                ArrayList<String> resumeMp4PathList = processResume();
                if (resumeMp4PathList == null) {
                    BYDLog.d(TAG, "process resumeMp4PathList is null");
                    break;
                }

                AutoVideoHandler.getInstance().sendMessage(SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_TFCARD_SCAN);
                BYDLog.d(TAG, "process resumeMp4PathList size " + resumeMp4PathList.size());
                // notify database update
                for (int i = 0; i < resumeMp4PathList.size(); i++) {
                    BYDLog.d(TAG, "processResume mp4Path " + resumeMp4PathList.get(i));
                    notifyDbFileChanged(resumeMp4PathList.get(i));
                }
                break;
            case MSG_RECORDER_NORMAL_STOP:
                Object obj = msg.obj;
                BYDLog.d(TAG, "process: obj is " + obj);
                if (obj != null && obj instanceof String) {
                    String name = (String) obj;
                    //Sub class implements.
                    String dest = processStop(name);
                    BYDLog.d(TAG, "process:  name return from processStop is " + dest);
                    if (dest != null && !dest.isEmpty() && new File(dest).exists()) {
                        BYDLog.d(TAG, "process: resume mp4 name " + name + " sucessful");
                        notifyDbFileChanged(dest);
                    } else {
                        BYDLog.e(TAG, "MSG_RECORDER_NORMAL_STOP process stop fail.");
                    }
                } else {
                    BYDLog.e(TAG, "MSG_RECORDER_NORMAL_STOP obj is invalid.");
                }
                break;
            default:
                BYDLog.w(TAG, "process: msg is " + msg.what);
        }
    }


    /**
     * List the path.
     *
     * @param path
     * @return
     */
    public static File[] list(String path) {
        if (path == null || path.isEmpty()) {
            BYDLog.w(TAG, "list: path is " + path);
            return null;
        }
        File p = new File(path);
        if (!p.isDirectory()) {
            BYDLog.w(TAG, "list: path[" + path + "] is not dir");
            return null;
        }
        return p.listFiles();
    }

    /**
     * if sd card eject,remove the MSG_RECORDER_NORMAL_STOP msg
     *
     * @return
     */
    public synchronized void removeRecorderNoramlStopMsg() {
        if (mInstance != null && mHandler != null) {
            if (mHandler.hasMessages(MSG_RECORDER_NORMAL_STOP)) {
                BYDLog.d(TAG, "removeRecorderNoramlStopMsg");
                mHandler.removeMessages(MSG_RECORDER_NORMAL_STOP);
            }
        }
    }

    /**
     * notify database changed
     *
     * @param filePath
     * @return
     */
    protected synchronized void notifyDbFileChanged(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        BYDLog.d(TAG, "notifyDbFileChanged file = " + filePath);
        BYDLog.d(TAG, "notifyDbFileChanged fileName = " + fileName);
        if (!SDCardStatus.getInstance().isTFlashCardExists()) {
            BYDLog.d(TAG, "notifyDbFileChanged cur recording name = " + fileName);
            BYDLog.d(TAG, "notifyDbFileChanged notifyDbFileChanged() SD card eject, return!");
            return;
        }
        int what = SentryModeHandlerMsg.SENTRYMODE_MSG_TYPE_RECOVER_VIDEO_END;
        BYDLog.d(TAG, "notifyDbFileChanged msg is = " + what);
        AutoVideoHandler.getInstance().sendDirectlyMessage(what, filePath);
    }


    /**
     * handler delete file
     *
     * @param copyPathName
     * @param desPathName：mp4PathName
     * @param delayMs
     */
    protected void handleDelCopy(final String copyPathName, final String desPathName, long delayMs) {
        this.postRunnable(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                BYDLog.d(TAG, "handleDelCopy: copyPathName = " + copyPathName + " desPathName = " + desPathName);
                if (desPathName.endsWith(".mp4")
                        && VideoMpeg4File.check(desPathName, MediaFormat.MIMETYPE_VIDEO_AVC)) {
                    BYDLog.d(TAG, "handleDelCopy: delete copyPathName = " + copyPathName);
                    new File(copyPathName).delete();//Msg
                } else {// if(desPathName.endsWith(".flv")){
                    BYDLog.e(TAG, "handleDelCopy: desPathName:des name is " + desPathName);
                    new File(desPathName).delete();//Msg
                }
            }
        }, delayMs);
    }

}
