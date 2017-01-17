package com.daigou.selfstation;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.module.annotations.ReactModule;

/**
 * Creator:HeXiangYuan
 * Date  : 17-1-16
 */
@ReactModule(name = "StartActivityAndroid")
public class StartActivityModule extends ReactContextBaseJavaModule {
    public StartActivityModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "StartActivityAndroid";
    }

    @ReactMethod
    public void dataToJS(Callback successBack, Callback errorBack) {
        try {
            Activity currentActivity = getCurrentActivity();
            String result = currentActivity.getIntent().getStringExtra("data");
            if (TextUtils.isEmpty(result)) {
                result = "没有数据";
            }
            successBack.invoke(result);
        } catch (Exception e) {
            errorBack.invoke(e.getMessage());
        }
    }
    @ReactMethod
    public void startActivityFromJSExtras(String activityPkgName, ReadableMap paramsMap) {
        try {
            Activity currentActivity = getCurrentActivity();
            if (null != currentActivity) {
                Class toActivity = Class.forName(activityPkgName);
                Intent intent = new Intent(currentActivity, toActivity);
                if(paramsMap != null){
                    ReadableMapKeySetIterator iterator = paramsMap.keySetIterator();
                    while(iterator.hasNextKey()){
                        String key = iterator.nextKey();
                        if (ReadableType.Number.equals(paramsMap.getType(key))){
                            intent.putExtra(key, paramsMap.getInt(key));
                        } else if (ReadableType.String.equals(paramsMap.getType(key))){
                            intent.putExtra(key,paramsMap.getString(key));
                        } else if (ReadableType.Boolean.equals(paramsMap.getType(key))){
                            intent.putExtra(key,paramsMap.getBoolean(key));
                        }
                    }
                }
                currentActivity.startActivity(intent);
            }
        } catch (Exception e) {
            throw new JSApplicationIllegalArgumentException(
                    "不能打开Activity : " + e.getMessage());
        }
    }
    @ReactMethod
    public void startActivityFromJS(String activityPkgName) {
        try {
            Activity currentActivity = getCurrentActivity();
            if (null != currentActivity) {
                Class toActivity = Class.forName(activityPkgName);
                Intent intent = new Intent(currentActivity, toActivity);
                currentActivity.startActivity(intent);
            }
        } catch (Exception e) {
            throw new JSApplicationIllegalArgumentException(
                    "不能打开Activity : " + e.getMessage());
        }
    }
}
