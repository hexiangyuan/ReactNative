package com.daigou.selfstation.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.daigou.selfstation.BuildConfig;
import com.daigou.selfstation.system.EzDeliveryApplication;
import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;

/**
 * Created by 何祥源 on 16/6/22.
 * Desc:蒲公英的自动更新;
 */
public class PgyManager {
    public static void register(final Activity context) {
        if (BuildConfig.DEBUG) {
            return;
        }
        PgyUpdateManager.register(context, new UpdateManagerListener() {
            @Override
            public void onNoUpdateAvailable() {

            }

            @Override
            public void onUpdateAvailable(String s) {
                // 将新版本信息封装到AppBean中
                final AppBean appBean = getAppBeanFromString(s);
                new AlertDialog.Builder(context)
                        .setTitle("Update")
                        .setMessage(appBean.getReleaseNote())
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(
                                android.R.string.ok,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        startDownloadTask(
                                                context,
                                                appBean.getDownloadURL());
                                    }
                                }).show();
            }
        });
    }

    /**
     * 需求：每次apk更新的时候都会清空数据重新登录；
     * @return
     */
    public static boolean isFirstUse(){
        int oldVersionCode = SharePreferenceUtils.getInt(EzDeliveryApplication.getInstance(), "versionCode", -1);
        if(BuildConfig.VERSION_CODE > oldVersionCode){
            return true;
        }else {
            return false;
        }
    }
}
