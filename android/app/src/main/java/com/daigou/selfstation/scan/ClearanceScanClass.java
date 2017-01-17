package com.daigou.selfstation.scan;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.R;
import com.daigou.selfstation.activity.ScanActivity;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.utils.ToastUtil;
import com.google.zxing.Result;
import com.google.zxing.client.android.PreferencesActivity;
import com.google.zxing.client.android.clipboard.ClipboardInterface;
import com.google.zxing.client.android.result.ResultHandler;

/**
 * Creator:HeXiangYuan
 * Date  : 17-1-5
 */

public class ClearanceScanClass extends ScanActivity {

    private RpcRequest request;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        findViewById(R.id.scan).setVisibility(View.GONE);
        inputCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                if (i == EditorInfo.IME_ACTION_DONE) {
                    clearance(inputCode.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
        CharSequence displayContents = resultHandler.getDisplayContents();
        if (copyToClipboard && !resultHandler.areContentsSecure()) {
            ClipboardInterface.setText(displayContents, this);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (resultHandler.getDefaultButtonID() != null && prefs.getBoolean(PreferencesActivity.KEY_AUTO_OPEN_WEB, false)) {
            resultHandler.handleButtonPress(resultHandler.getDefaultButtonID());
            return;
        }
        if (barcode != null) {
            barcodeImageView.setImageBitmap(barcode);
        }
        clearance(rawResult.getText());
    }

    private void clearance(String text) {
        request = DeliveryService.UserCleanSGParcel(text, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                restartPreviewAfterDelay(2000);//2s后继续扫描
                if (response != null) {
                    if ("".equals(response)) {
                        showSuccess();
                    }
                } else {
                    ToastUtil.showToast(R.string.Network_is_error);
                }
            }
        });
    }

    private void showSuccess() {
        mpSucceeded.start();
        ToastUtil.showToast(getString(R.string.clearMsg));
    }

    @Override
    protected void onDestroy() {
        if (request != null)
            request.cancel();
        super.onDestroy();
    }
}
