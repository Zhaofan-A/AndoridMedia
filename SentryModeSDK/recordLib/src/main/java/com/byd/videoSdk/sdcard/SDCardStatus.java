package com.byd.videoSdk.sdcard;

import android.os.storage.StorageManager;

import com.byd.videoSdk.recorder.RecorderController;
import com.byd.videoSdk.common.util.BYDLog;

import java.io.File;
import java.util.Vector;

public class SDCardStatus {
    private static final String TAG = "SDCardStatus";

    public static final int SDCARD_EJECT = 1000;
    public static final int SDCARD_MOUNTED = 1001;
    public static final int SDCARD_LOW_PERFORMANCE = 1002;
    public static final int SDCARD_NORMAL_PERFORMANCE = 1003;

    public static SDCardStatus mInstance = null;
    private StorageManager mStorageManager;

    private Vector<SDCardStatusListener> mSDCardStatusListener = new Vector<SDCardStatusListener>();

    private SDCardStatus() {
        mStorageManager = RecorderController.getInstance().getContext().getSystemService(StorageManager.class);
    }

    /**
     * Get and create the instance.
     *
     * @return
     */
    public synchronized static SDCardStatus getInstance() {
        if (mInstance == null) {
            mInstance = new SDCardStatus();
        }
        return mInstance;
    }

    /**
     * sd card is exist or not
     */
    public boolean isTFlashCardExists() {
//        if (BydFileUtils.getSavePathDebug()) {
            return true;
//        }
//        boolean sdExist = SystemProperties.getBoolean("sys.byd.isSDExist", false);
//        BYDLog.d(TAG, "sdExist = " + sdExist);
//        return sdExist;
    }

    public boolean isExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        return true;
    }


    /*
     * Add for tf low performance.
     */
    private boolean isTFLowPerformance = false;
    /**
     * Set the tf low performace
     * @param lowPerformace
     */
    public void setIsTFLowPerformance(boolean lowPerformace){
        if(isTFLowPerformance != lowPerformace){
            isTFLowPerformance = lowPerformace;
            if(isTFLowPerformance){
                setSDCardState(SDCARD_LOW_PERFORMANCE);
            }else{
                setSDCardState(SDCARD_NORMAL_PERFORMANCE);
            }
        }
    }
    /**
     * TF is low performance or not.
     * @return
     */
    public boolean isTFLowPerformance(){
        return isTFLowPerformance;
    }
    /*End add*/

    /**
     * Register the state changed listener to listen the sdcard status changed.
     *
     * @param l
     */
    public void registerSDStateChangedListener(SDCardStatusListener l) {
        if (!mSDCardStatusListener.contains(l)) {
            BYDLog.d(TAG, "register SDState Listener  size = " + mSDCardStatusListener.size());
            mSDCardStatusListener.add(l);
        }
    }

    /**
     * Unregister the state changed listener to listen the sdcard status changed.
     *
     * @param l
     */
    public void unregisterSDStateChangedListener(SDCardStatusListener l) {
        if (mSDCardStatusListener.contains(l)) {
            mSDCardStatusListener.remove(l);
        }
    }

    public void setSDCardState(int sdState) {
        for (int i = 0; i < mSDCardStatusListener.size(); i++) {
            SDCardStatusListener listener = mSDCardStatusListener.get(i);
            if (listener != null) {
                listener.onSDCardStateChanged(sdState);
            }
        }
    }

    /**
     * To listen the sdcard status changed.
     *
     * @author zhangjuan
     */
    public static interface SDCardStatusListener {
        /**
         * sdcard state changed
         *
         * @param state
         * @return
         */
        public void onSDCardStateChanged(int state);
    }

    ;
}

