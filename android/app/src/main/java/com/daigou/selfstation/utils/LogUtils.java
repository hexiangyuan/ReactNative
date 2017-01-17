package com.daigou.selfstation.utils;


import com.daigou.selfstation.BuildConfig;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

/**
 * Created by 何祥源 on 16/8/29.
 * Desc:
 */
public class LogUtils {

    public static void loggerInit() {
        if (BuildConfig.DEBUG) {
            Logger
                    .init("ezbuy")
                    .methodCount(2)
                    .logLevel(com.orhanobut.logger.LogLevel.FULL)
                    .hideThreadInfo()// default shown
                    .methodOffset(2);
        } else {
            Logger
                    .init("ezbuy")
                    .methodCount(2)
                    .logLevel(LogLevel.NONE) // default 2
                    .hideThreadInfo()               // default shown
                    .methodOffset(2);                // default 0
        }
    }

    public static void v(String tag, String msg) {
        Logger.t(tag).v(msg);
    }

    public static void i(String tag, String msg) {
        Logger.t(tag).i(msg);
    }

    public static void d(String tag, String msg) {
        Logger.t(tag).d(msg);
    }

    public static void e(String tag, String msg) {
        Logger.t(tag).e(msg);
    }

    public static void w(String tag, String msg) {
        Logger.t(tag).w(msg);
    }

    public static void json(String tag, String json) {
        Logger.t(tag).json(json);
    }

    public static void xml(String tag, String xml) {
        Logger.t(tag).e(xml);
    }
}
