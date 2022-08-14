package com.byd.videoSdk.recorder.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 这个是SharedPreferences的封装
 */

public class SharedPreferencesManager {

    private static SharedPreferencesManager spManager = null;
    private static SharedPreferences sp = null;
    private static SharedPreferences.Editor editor = null;

    private static final String SHARE_NAME = "sentrymode_recorder";//Preference文件名

    private SharedPreferencesManager(Context context) {
        sp = context.getSharedPreferences(SHARE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public static SharedPreferencesManager getInstance(Context context) {
        if (spManager == null || sp == null || editor == null) {
            spManager = new SharedPreferencesManager(context);
        }
        return spManager;
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public int getInt(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    public void putLong(String key, Long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public long getLong(String key, int defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public void putFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public boolean isKeyExist(String key) {
        return sp.contains(key);
    }

    public float getFloat(String key, float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }
}