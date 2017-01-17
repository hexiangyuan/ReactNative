package com.daigou.selfstation.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.daigou.selfstation.R;
import com.google.zxing.Result;

/**
 * Created by 何祥源 on 16/4/27.
 * Desc:
 */
public class SignScanActivity extends ScanActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        RadioButton radioButton = (RadioButton) findViewById(R.id.pick_up_rb);
        findViewById(R.id.parcel_rb).setVisibility(View.INVISIBLE);
        findViewById(R.id.shelf_rb).setVisibility(View.INVISIBLE);
        radioButton.setVisibility(View.INVISIBLE);
        radioButton.setChecked(true);
    }

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        super.handleDecode(rawResult, barcode, scaleFactor);
    }
}
