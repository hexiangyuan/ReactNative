package com.daigou.selfstation.webview;

/**
 * Created by 何祥源 on 16/6/2.
 * Desc:
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;


/**
 * Created by ：wangxiang on 16/5/25
 */
public class EzWebView extends WebView {
    public EzWebView(Context context) {
        super(context);
    }

    public EzWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EzWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EzWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void loadUrl(String url, String cookieKey, String cookieValue, String domain, String path) {
        setCookie(url, cookieKey, cookieValue, domain, path);
        super.loadUrl(url);
    }

    public void setCookie(String url, String cookieKey, String cookieValue, String domain, String path) {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookieHeader = getCookieHeader(cookieKey, cookieValue, domain, path);
        if (Build.VERSION.SDK_INT < 21) {
            CookieSyncManager.createInstance(getContext());
            cookieManager.setCookie(url, cookieHeader);
            CookieSyncManager.getInstance().sync();
        } else {
            cookieManager.acceptCookie();
            cookieManager.setCookie(url, cookieHeader);
            cookieManager.flush();
        }
    }


    public static String getCookieHeader(String cookieKey, String cookieValue, String domain, String path) {
        return cookieKey + "=" + cookieValue + "; domain=" + domain + "; path=" + path;
    }
}
