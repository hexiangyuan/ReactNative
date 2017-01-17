package com.daigou.selfstation.utils;

import android.os.Message;

import com.daigou.selfstation.system.EzDeliveryApplication;

/**
 * Created by ：wangxiang on 15/10/15
 */
public class NetworkError {
    private NetworkError() {
    }

    /**
     * 网络连接失败
     */
    public static void unKnowHost() {
        Message notLogin = new Message();
        notLogin.what = EzDeliveryApplication.UN_KNOW_HOST;
        EzDeliveryApplication.APP_HANDLER.handleMessage(notLogin);
    }

    /**
     * 处理不同的http response status code
     *
     * @param responseStatusCode
     */
    public static void networkError(int responseStatusCode) {
        switch (responseStatusCode) {
            case EzDeliveryApplication.NOT_LOGIN:
                status401();
                break;
        }
    }

    /**
     * 未登录
     */
    public static void status401() {
        Message notLogin = new Message();
        notLogin.what = EzDeliveryApplication.NOT_LOGIN;
        EzDeliveryApplication.APP_HANDLER.handleMessage(notLogin);
    }
}
