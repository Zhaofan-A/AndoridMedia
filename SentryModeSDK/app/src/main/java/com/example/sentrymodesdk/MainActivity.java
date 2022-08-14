package com.example.sentrymodesdk;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    private Intent mIntent;
    private RadioGroup record_duration_group;
    private RadioGroup watermarkGroup;
    private RadioGroup recoverEnableGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        record_duration_group = (RadioGroup) findViewById(R.id.record_duration_group);
        watermarkGroup = (RadioGroup) findViewById(R.id.watermarkGroup);
        recoverEnableGroup = (RadioGroup) findViewById(R.id.recoverEnableGroup);
        record_duration_group.setOnCheckedChangeListener(onCheckedChangeListener);
        watermarkGroup.setOnCheckedChangeListener(onCheckedChangeListener);
        recoverEnableGroup.setOnCheckedChangeListener(onCheckedChangeListener);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                "android.hardware.camera",
                "android.hardware.camera.autofocus"
        }, 5);
        mIntent = new Intent();
    }

    RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (group.getId()) {
                case R.id.record_duration_group:
                    Log.d(TAG, "onCheckedChanged: record_duration_group " + ((RadioButton) findViewById(checkedId)).getText());
                    switch (checkedId) {
                        case R.id.time30s:
                            SdkDemoApplication.mDuration = 30;
                            break;
                        case R.id.time60s:
                            SdkDemoApplication.mDuration = 60;
                            break;
                        case R.id.time3m:
                            SdkDemoApplication.mDuration = 3 * 60;
                            break;
                        default:
                            break;
                    }
                    Log.d(TAG, "mDuration: " + SdkDemoApplication.mDuration);
                    break;
                case R.id.watermarkGroup:
                    switch (checkedId) {
                        case R.id.watermarkYes:
                            SdkDemoApplication.mWatermark = true;
                            break;
                        case R.id.watermarkNo:
                            SdkDemoApplication.mWatermark = false;
                            break;
                        default:
                            break;
                    }
                    Log.d(TAG, "mWatermark: " + SdkDemoApplication.mWatermark);
                    break;
                case R.id.recoverEnableGroup:
                    Log.d(TAG, "onCheckedChanged: ");
                    switch (checkedId) {
                        case R.id.recoverYes:
                            SdkDemoApplication.mRecorver = true;
                            break;
                        case R.id.recoverNo:
                            SdkDemoApplication.mRecorver = false;
                            break;
                        default:
                            break;
                    }
                    Log.d(TAG, "mRecorver: " + SdkDemoApplication.mRecorver);
                    break;
                default:
                    break;
            }
        }
    };

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.record:
                mIntent.setClass(this, PreviewRecordActivity.class);
                startActivity(mIntent);
                break;
            case R.id.recode2:
                startActivity(new Intent(this, RecordNoPreviewActivity.class));
                break;
            default:
                break;
        }
    }

}
