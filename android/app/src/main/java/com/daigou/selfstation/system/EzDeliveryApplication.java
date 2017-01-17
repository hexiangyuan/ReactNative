package com.daigou.selfstation.system;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.daigou.model.TRpc;
import com.daigou.selfstation.BuildConfig;
import com.daigou.selfstation.activity.LoginActivity;
import com.daigou.selfstation.rpc.selfstation.TLoginResult;
import com.daigou.selfstation.utils.LogUtils;
import com.daigou.selfstation.utils.SharePreferenceUtils;

import io.fabric.sdk.android.Fabric;

/**
 * Created by ：wangxiang on 15/10/12
 */
public class EzDeliveryApplication extends android.app.Application {
    public static final int NOT_LOGIN = 401;
    public static final int UN_KNOW_HOST = 1231231;

    public static class AppHandler extends Handler {
        EzDeliveryApplication mEzDeliveryApplication;

        public AppHandler(EzDeliveryApplication ezDeliveryApplication) {
            mEzDeliveryApplication = ezDeliveryApplication;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NOT_LOGIN:
                    mEzDeliveryApplication.toLogin();
                    break;
                case UN_KNOW_HOST:
                    mEzDeliveryApplication.unKnowHost();
                    break;
            }
        }
    }

    public static AppHandler APP_HANDLER;
    private static EzDeliveryApplication INSTANCE;
    private static RequestQueue QUEUE;
    private TLoginResult loginResult;

    public TLoginResult getLoginResult() {
        return loginResult;
    }

    public void setLoginResult(TLoginResult loginResult) {
        this.loginResult = loginResult;
    }

    public static EzDeliveryApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        APP_HANDLER = new AppHandler(this);
        INSTANCE = this;
        QUEUE = Volley.newRequestQueue(this);
        LogUtils.loggerInit();
        initRPC();
    }

    public void toLogin() {
        loginResult = null;
        LoginActivity.deleteLoginInfo(INSTANCE);
        Intent i = new Intent(INSTANCE, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        INSTANCE.startActivity(i);
    }

    private void initRPC() {
        TRpc.builder builder = new TRpc.builder();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        AppUrl.setIsLiving(sp.getBoolean("mode", !BuildConfig.DEBUG));
        String serverUrl = SharePreferenceUtils.getString(EzDeliveryApplication.this, "serverUrl", AppUrl.kJsonRpcCoreUrl);
        builder.setJsonRpcUrl(TextUtils.isEmpty(serverUrl) ? AppUrl.kJsonRpcCoreUrl : serverUrl)
                .setWebApiUrl(TextUtils.isEmpty(serverUrl) ? AppUrl.kJsonRpcCoreUrl : serverUrl)
                .setQueue(QUEUE)
                .setCustomerCookie(PreferenceManager.getDefaultSharedPreferences(this).getString("token", ""))
                .setLogJson(new TRpc.ILogJson() {
                    @Override
                    public void LogJson(String s) {
                        LogUtils.json("ezbuyRpc", s);
                    }
                })
                .setVolleyError(new TRpc.IVolleyError() {
                    @Override
                    public void onVolleyError(VolleyError volleyError) {

                    }
                })
                .build();
    }

    public void unKnowHost() {
        Toast.makeText(INSTANCE, "Network not available", Toast.LENGTH_LONG).show();
    }

    public void setCookie(String cookie) {
        TRpc.getInstance().setCustomerCookie(cookie);
    }

    public String getCookie() {
        return TRpc.getInstance().getCustomerCookie();
    }

    public void setWebApiUrl(String str) {
        TRpc.getInstance().setWebApiUrl(str);
    }
}
