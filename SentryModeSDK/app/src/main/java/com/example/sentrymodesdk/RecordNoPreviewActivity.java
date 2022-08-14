package com.example.sentrymodesdk;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.byd.videoSdk.common.util.BydFileUtils;
import com.byd.videoSdk.recorder.RecorderController;
import com.byd.videoSdk.recorder.listener.OnAddWatermarkListener;
import com.byd.videoSdk.recorder.utils.RecordConfig;
import com.byd.videoSdk.common.util.ShaderUtil;
import com.byd.videoSdk.watermark.WatermarkBitmapInfo;
import com.byd.videoSdk.watermark.WatermarkTextInfo;
import com.byd.videoSdk.watermark.WatermarkTimeInfo;

public class RecordNoPreviewActivity extends AppCompatActivity {
    private static final String TAG = "RecordNoPreviewActivity";

    private static final int RECORD_TIME_SHOW = 0;

    private Button button;
    private TextView recordTimeShow;
    private TextView recordPath;

    RecorderController   mRecorderController;
    private int screenWidth;
    private int screenHeight;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nopreview_recorde);
        button = findViewById(R.id.recode);
        recordTimeShow = (TextView)findViewById(R.id.recordTimeShow);
        recordPath =  (TextView)findViewById(R.id.recordPath);

        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();

        mRecorderController = RecorderController.getInstance();
    }

    public void recode(View view) {
        Log.d(TAG, "recode: ");
        if (!mRecorderController.isStartRecord()) {
            startRecode();
            button.setText("正在录制");
        } else {
            mRecorderController.stopRecord();
            button.setText("开始录制");
        }
    }

    public void capature(View view) {
        //开启录像了后才可以拍照
        mRecorderController.takingPhoto();
    }


    private void startRecode() {
        mRecorderController.setConfig(new RecordConfig(screenWidth, screenHeight));
        mRecorderController.setRecordTime(SdkDemoApplication.mDuration);
        mRecorderController.setRecorverEnable(SdkDemoApplication.mRecorver);
        mRecorderController.setSavePath(BydFileUtils.getFilesPath(this));
        recordPath.setText(BydFileUtils.getFilesPath(this));

        if(SdkDemoApplication.mWatermark){
            mRecorderController.starNoPreviewRecord("testRecodeNoPreview.mp4",onAddWatermarkListener);
        }else{
            mRecorderController.starNoPreviewRecord("testRecodeNoPreview.mp4",null);
        }
        handler.sendEmptyMessage(RECORD_TIME_SHOW);
    }


    OnAddWatermarkListener onAddWatermarkListener= new OnAddWatermarkListener() {
        @Override
        public void onAddWatermark() {
            String textColor = "#fff000";
            String bgColor = "#00000000";
            int textSize = 60;
            Bitmap bitmap = ShaderUtil.createTextImage("图片水印",textSize,textColor,bgColor,0);
            mRecorderController.addWatermarkBitmap(new WatermarkBitmapInfo(bitmap,50,400));
            mRecorderController.addWatermarkText(new WatermarkTextInfo("文字水印测试",50,600));
            mRecorderController.addWatermarkTime(new WatermarkTimeInfo());
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mRecorderController.stopRecord();
    }

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case RECORD_TIME_SHOW:
                    recordTimeShow.setText(mRecorderController.getAlreadRecordTime()+"秒");
                    handler.sendEmptyMessageDelayed(RECORD_TIME_SHOW,1000);
                    break;
                default:break;
            }
        }
    };


}
