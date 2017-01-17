package com.daigou.selfstation.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.daigou.selfstation.R;
import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;

/**
 * Creator:HeXiangYuan
 * Date  : 16-11-29
 */

public class SearchSubParcelActivity extends CaptureActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        findViewById(R.id.scan).setVisibility(View.GONE);
        EditText inputCode = (EditText) findViewById(R.id.input_code);
        inputCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                intentToList(textView.getText().toString());
                return true;
            }
        });

    }

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        Log.e("abcd", rawResult.getText());
        if (!TextUtils.isEmpty(rawResult.getText())) {
            intentToList(rawResult.getText());
        }
    }

    private void intentToList(String text) {
        Intent i = new Intent(this, ScanSearchShowActivity.class);
        i.putExtra("subPackage", text);
        startActivity(i);
    }
}
