package com.daigou.selfstation.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by 何祥源 on 16/6/16.
 * Desc:这是sp的保存类用来存取SharePreference
 */
public class SharePreferenceUtils {

    public static SharedPreferences getDefaultPreference(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * 添加bool类型的prefrence；
     *
     * @param context
     * @param key
     * @param value
     * @return 是否成功
     */
    public static boolean putBool(Context context, String key, Boolean value) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).commit();
    }

    /**
     * 获取prefrence的Bool类型的数值；
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return value值 boolean 类型
     */
    public static boolean getBool(Context context, String key, Boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }

    /**
     * 添加Int类型的值
     *
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean putInt(Context context, String key, int value) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).commit();
    }

    /**
     * 获取Int类型的值
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static int getInt(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue);
    }

    /**
     * 添加String类型的值
     *
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean putString(Context context, String key, String value) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).commit();
    }

    /**
     * 获取String类型的值
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getString(Context context, String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
    }
}


