package com.example.jnilearn2;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements SurfaceHolder.Callback{

    private final static String TAG = "camera-video";
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private android.hardware.Camera camera=null;
    // camera
    private Button cam0Button,testButton;
    private ImageView imageView;
    private boolean opened = false;
    private int count =0;
    private float scale = 1.0f;
    // Used to load the 'native-lib' library on application startup.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        surfaceView.setFocusable(true);
        surfaceView.setFocusableInTouchMode(true);
        surfaceView.setClickable(true);
        holder = surfaceView.getHolder();
        holder.addCallback(this);

        cam0Button = (Button) findViewById(R.id.start);
        imageView = (ImageView)findViewById(R.id.imageView1);

        cam0Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (camera == null) {
                    if (holder != null) {
                        openCamera(0, mPreviewCallback);
                    }
                }
            }
        });
        // Example of a call to a native method
        //TextView tv = findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());
        NativeLibrary.getInstance().init(2560,480,scale);
    }

    @Override
    protected void onDestroy() {
        NativeLibrary.getInstance().destroy();
        super.onDestroy();
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    @SuppressLint("NewApi")
    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        holder = arg0;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        stopCamera();
    }


    private void stopCamera(){
        if(camera != null){
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            opened = false;
        }
    }


    @SuppressLint("NewApi")
    private void openCamera(int id, Camera.PreviewCallback cb){
        id = 0;
        if(opened){
            camera.stopPreview();
            camera.release();
            opened = false;
        }
        Log.i(TAG,"ready to open camera");
        camera = android.hardware.Camera.open(id);
        Log.i(TAG,"open camera");
        opened = true;
        try {
            camera.setPreviewCallback(cb);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            Toast.makeText(this,"camera:"+id+" open failed", Toast.LENGTH_SHORT).show();
            opened = false;
            Log.i(TAG,e.toString());
        }
        Log.d(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!start preview!!!!!!!!!!!!!!!!!!!!!");
    }

    Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback(){
        @Override
        public void onPreviewFrame(byte[] arg0, Camera arg1) {
            Log.d("suzhiyong", "camera data length is "+ arg0.length);
            count++;

            if(opened && (count==10))
            {
               int[] resultPixes = NativeLibrary.getInstance().detection(arg0,2560,480);
                //int[] resultPixes = NativeLibrary.getInstance().grey(arg0,2560,480);
                //boolean result = NativeLibrary.getInstance().detection2(arg0,2560,480);
                Bitmap result = Bitmap.createBitmap((int)(2560*scale),(int)(480*scale),Bitmap.Config.RGB_565);
                result.setPixels(resultPixes,0,(int)(2560*scale),0,0,(int)(2560*scale),(int)(480*scale));
                imageView.setImageBitmap(result);
                count = 0;
            }

        }
    };

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

}
