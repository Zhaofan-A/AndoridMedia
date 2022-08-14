package com.byd.videoSdk.watermark;

import android.content.Context;
import android.util.Log;

import com.byd.videoSdk.common.util.BYDLog;

import java.util.HashMap;

public class WatermarkFactor {
    private static final String TAG = "WatermarkFactor";

    public static final String PREVIEW_WATERMARK_RENDER = "PREVIEW_WATERMARK_RENDER";
    public static final String RECORD_WATERMARK_RENDER = "RECORD_WATERMARK_RENDER";

    private Context mContext;
    private static WatermarkFactor watermarkFactor;
    private HashMap<String, WatermarkRender> watermarkRenderMap = new HashMap<>();


    private WatermarkFactor(Context context) {
        mContext = context;
    }

    public static WatermarkFactor getInstance(Context context) {
        if (watermarkFactor == null) {
            watermarkFactor = new WatermarkFactor(context);
        }
        return watermarkFactor;
    }

    public WatermarkRender getWatermarkRender(String type) {
        BYDLog.d(TAG, "getWatermarkRender: type = " + type);
        if (!watermarkRenderMap.containsKey(type)) {
            BYDLog.d(TAG, "getWatermarkRender: new object");
            WatermarkRender watermarkRender = new WatermarkRender(mContext, type);
            watermarkRenderMap.put(type, watermarkRender);
        }
        WatermarkRender watermarkRender = watermarkRenderMap.get(type);
        if (type.equals(RECORD_WATERMARK_RENDER)) {   //保证预览画面的水印和录像的画面水印一致
            WatermarkRender previewWatermarkRender = watermarkRenderMap.get(PREVIEW_WATERMARK_RENDER);
            BYDLog.d(TAG, "getWatermarkRender: previewWatermarkRender = " + previewWatermarkRender);
            if (watermarkRender != null && previewWatermarkRender != null) {
                if (previewWatermarkRender.getWatermarkBitmpList().size() != 0) {
                    for (WatermarkBitmapInfo bitmapInfo : previewWatermarkRender.getWatermarkBitmpList()) {
                        watermarkRender.addWatermarkBitmap(bitmapInfo);
                    }
                }
                if (previewWatermarkRender.getWatermarkTextList().size() != 0) {
                    for (WatermarkTextInfo textInfo : previewWatermarkRender.getWatermarkTextList()) {
                        watermarkRender.addWatermarkText(textInfo);
                    }
                }
                if (previewWatermarkRender.getWatermarkTimeList().size() != 0) {
                    for (WatermarkTimeInfo timeInfo : previewWatermarkRender.getWatermarkTimeList()) {
                        watermarkRender.addWatermarkTime(timeInfo);
                    }
                }
            }
        }
        return watermarkRender;
    }

    protected void removeWatermarkRender(String type) {
        watermarkRenderMap.remove(type);
    }

}
