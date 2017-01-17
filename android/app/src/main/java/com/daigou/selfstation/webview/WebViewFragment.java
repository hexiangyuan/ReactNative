package com.daigou.selfstation.webview;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.daigou.selfstation.system.AppUrl;
import com.daigou.selfstation.system.EzDeliveryApplication;


/**
 * Created by ：wangxiang on 16/6/1
 */
public class WebViewFragment extends Fragment {

    private EzWebView mWebView;
    private String url;


    public static WebViewFragment newInstance(Bundle args) {
        WebViewFragment res = new WebViewFragment();
        res.setArguments(args);
        return res;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mWebView = new EzWebView(getActivity());
        return mWebView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        url = getArguments() != null ? getArguments().getString("url") : null;
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setSupportZoom(false); //不支持页面放大功能
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.setCookie(url, "65_customer", EzDeliveryApplication.getInstance().getCookie(), AppUrl.getDOMAIN(), "/");
    }

    @Override
    public void onDestroy() {
        if (mWebView != null) {
            mWebView.pauseTimers();
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    public static Bundle setArguments(String ezbuyProtocol) {
        Bundle bundle = new Bundle();
        bundle.putString("url", ezbuyProtocol);
        return bundle;
    }
}
