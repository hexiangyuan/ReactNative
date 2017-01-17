package com.daigou.selfstation.webview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.daigou.selfstation.R;
import com.daigou.selfstation.activity.EzBaseActivity;

/**
 * Created by 何祥源 on 16/6/2.
 * Desc:
 */
public class WebViewActivity extends EzBaseActivity {
    private String url;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout container = new FrameLayout(this);
        container.setId(R.id.contain);
        setContentView(container);
        url = getIntent().getStringExtra("url");
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        WebViewFragment webViewFragment = WebViewFragment.newInstance(WebViewFragment.setArguments(url));
        ft.add(R.id.contain, webViewFragment, "webView");
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (fragmentManager.getBackStackEntryCount() <= 1) {
            finish();
        } else {
            fragmentManager.popBackStack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (fragmentManager.getBackStackEntryCount() <= 1) {
                    finish();
                } else {
                    fragmentManager.popBackStack();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static Bundle setArguments(String url) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        return bundle;
    }
}
