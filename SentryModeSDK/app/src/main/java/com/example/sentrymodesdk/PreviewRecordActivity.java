package com.example.sentrymodesdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.byd.videoSdk.camera.CameraEglSurfaceView;
import com.byd.videoSdk.common.util.BydFileUtils;
import com.byd.videoSdk.recorder.RecorderController;
import com.byd.videoSdk.recorder.listener.VideoStateListener;
import com.byd.videoSdk.recorder.utils.RecordConfig;
import com.byd.videoSdk.watermark.WatermarkBitmapInfo;
import com.byd.videoSdk.watermark.WatermarkTextInfo;
import com.byd.videoSdk.watermark.WatermarkTimeInfo;


public class PreviewRecordActivity extends AppCompatActivity {
    private static final String TAG = "PreviewRecordActivity";

    private CameraEglSurfaceView cameraEglSurfaceView;
    private Button button,loopRecode;

    RecorderController   mRecorderController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_recode);
        cameraEglSurfaceView = findViewById(R.id.camera_view);
        button = findViewById(R.id.recode);
        loopRecode = findViewById(R.id.loopRecode);

        if(SdkDemoApplication.mWatermark){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            cameraEglSurfaceView.addWatermarkBitmap(new WatermarkBitmapInfo(bitmap,50,400));
            cameraEglSurfaceView.addWatermarkText(new WatermarkTextInfo("文字水印测试",50,600));
            cameraEglSurfaceView.addWatermarkTime(new WatermarkTimeInfo());

            cameraEglSurfaceView.addWatermarkTime(new WatermarkTimeInfo( "#00000000","#EBF4FE",60,50,500));
        }

        mRecorderController = RecorderController.getInstance();
        mRecorderController.setSavePath(BydFileUtils.getFilesPath(this));
        mRecorderController.setRecorverEnable(SdkDemoApplication.mRecorver);

        mRecorderController.registerVideoStateListener(new VideoStateListener() {
            @Override
            public void onVideoStatusChange(boolean isLoopVideo, String videoName, int status) {
                Log.d(TAG, "onVideoStatusChange:  isLoop = " + isLoopVideo);
                Log.d(TAG, "onVideoStatusChange:  videoName = " + videoName);
                Log.d(TAG, "onVideoStatusChange:  state = " + status);
                String text = status == VideoStateListener.VIDEO_START ? "正在录制":"开始录制";
                if(isLoopVideo){
                     loopRecode.setText(text);
                }else{
                    button.setText(text);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        cameraEglSurfaceView.onDestroy();
        mRecorderController.stopRecord();
        mRecorderController.stopLoopRecord();
    }

    public void record(View view) {
        Log.d(TAG, "record: "+mRecorderController.isStartRecord());
        if (!mRecorderController.isStartRecord()) {
            startRecode();
            button.setText("正在录制");
        } else {
            mRecorderController.stopRecord();
            button.setText("开始录制");
        }
    }

    public void capature(View view) {
        boolean result = cameraEglSurfaceView.takingPhoto();
//        mRecorderController.takingPhoto();
        Toast.makeText(this,"拍照成功",Toast.LENGTH_SHORT).show();
    }

    private void startRecode( ) {
        Log.d(TAG, "startRecode: "+cameraEglSurfaceView.getCameraPrivewHeight()+"   "+cameraEglSurfaceView.getCameraPrivewWidth());
        mRecorderController.setRecordTime(SdkDemoApplication.mDuration);
        mRecorderController.setConfig(new RecordConfig(cameraEglSurfaceView.getCameraPrivewHeight(),cameraEglSurfaceView.getCameraPrivewWidth()));
        mRecorderController.starRecord("testRecodePreview.mp4",cameraEglSurfaceView);
    }

    private void startLoopRecode( ) {
        Log.d(TAG, "startLoopRecode: "+cameraEglSurfaceView.getCameraPrivewHeight()+"   "+cameraEglSurfaceView.getCameraPrivewWidth());
        mRecorderController.setLoopRecordTime(SdkDemoApplication.mDuration);
        mRecorderController.setConfig(new RecordConfig(cameraEglSurfaceView.getCameraPrivewHeight(),cameraEglSurfaceView.getCameraPrivewWidth()));
        mRecorderController.starLoopRecord("loop",cameraEglSurfaceView);
    }

    public void loopRecode(View view) {
        Log.d(TAG, "loopRecode: "+mRecorderController.isLoopStartRecord());
        if (!mRecorderController.isLoopStartRecord()) {
            startLoopRecode();
            loopRecode.setText("正在录制");
        } else {
            mRecorderController.stopLoopRecord();
            loopRecode.setText("开始录制");
        }
    }
}
