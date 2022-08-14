package com.byd.videoSdk.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

public class BydFileUtils {

    public static void saveFile(Bitmap bitmap, String name) {
        File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File file = new File(externalStoragePublicDirectory, name);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            quietClose(fileOutputStream);
        }
    }

    public static void quietClose(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void saveRgb2Bitmap(ByteBuffer buf, File file, int width, int height) {
        Log.d("TryOpenGL", "Creating " + file.getName());
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buf);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bmp.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void savePhoto(final ByteBuffer buffer, int width, int height, String savePath) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        Matrix matrix = new Matrix();
        matrix.postScale(1, -1);   //镜像垂直翻转
        Bitmap endBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

        File folder = new File(savePath);
        if (!folder.exists() && !folder.mkdirs()) {
            Log.e("demos", "图片目录异常");
            return;
        }
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String dateStr = dateformat.format(System.currentTimeMillis());

        String filePath = savePath + "PHO_" + dateStr + ".jpg";
        BufferedOutputStream bos = null;
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            bos = new BufferedOutputStream(fos);
            endBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.flush();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bitmap != null) {
                bitmap.recycle();
            }
            if (endBitmap != null) {
                endBitmap.recycle();
            }
        }
    }

    public static String getFilesPath(Context context) {
        String filePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            //外部存储可用
            filePath = context.getExternalFilesDir(null).getPath();
        } else {
            //外部存储不可用
            filePath = context.getFilesDir().getPath();
        }
        return filePath;
    }

}
