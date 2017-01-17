package com.daigou.selfstation.system;

import android.util.Log;

import com.qiniu.android.common.Zone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by ：wangxiang on 15/10/12
 */
public class QiNiuConfiguration {
    public static UploadManager uploadManager;
    private static String token;
    private static String key;

    public static void setToken(String token) {
        QiNiuConfiguration.token = token;
    }

    public static void init() {
        Configuration config = new Configuration.Builder()
                .chunkSize(256 * 1024)  //分片上传时，每片的大小。 默认 256K
                .putThreshhold(512 * 1024)  // 启用分片上传阀值。默认 512K
                .connectTimeout(10) // 链接超时。默认 10秒
                .responseTimeout(60) // 服务器响应超时。默认 60秒
                .zone(Zone.zone0) // 设置区域，指定不同区域的上传域名、备用域名、备用IP。默认 Zone.zone0
                .build();
        // 重用 uploadManager。一般地，只需要创建一个 uploadManager 对象
        uploadManager = new UploadManager(config);
    }

    public static String getKey() {
        return key;
    }

    private static void setKey(String key) {
        QiNiuConfiguration.key = key;
    }

    public static void upload(File file) {
        if (uploadManager == null) {
            init();
        }
        uploadManager.put(file, null, token,
                new UpCompletionHandler() {
                    public void complete(String key, ResponseInfo info, JSONObject res) {
                        if (res != null) {
                            try {
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res.getString("key"));
                                setKey(res.getString("key"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, null);
        // 签名图片上传地址为：http://7xiata.com1.z0.glb.clouddn.com/ + key的值
    }
}
