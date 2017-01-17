package com.daigou.selfstation.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.daigou.selfstation.R;
import com.daigou.selfstation.pick.ui.PickingActivity;
import com.daigou.selfstation.rpc.selfstation.TSubPackage;
import com.google.zxing.Result;
import com.google.zxing.client.android.PreferencesActivity;
import com.google.zxing.client.android.clipboard.ClipboardInterface;
import com.google.zxing.client.android.result.ResultHandler;

import java.util.ArrayList;

/**
 * Created by 何祥源 on 16/5/4.
 * Desc:
 */
public class ScanParcel2BoxActivity extends ScanActivity implements CompoundButton.OnCheckedChangeListener {
    protected RadioButton rbtnBox, rbtnParcel;
    protected TextView tvBoxNum, tvParcelNumbs;
    public static final int REQUEST_CODE = 200;
    private ArrayList<String> parcels = new ArrayList<>();
    private ArrayList<TSubPackage> searchResult = new ArrayList<>();
    private String boxNo;
    private StringBuilder sb = new StringBuilder();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_scan_parcel2box);
        findViewById(R.id.scan).setVisibility(View.GONE);
        if (getIntent() == null) {
            finish();
        }
        getSearchPkgs();

        tvBoxNum = (TextView) findViewById(R.id.box_num);
        tvParcelNumbs = (TextView) findViewById(R.id.parcel_num);
        inputCode = (EditText) findViewById(R.id.input_code);
        rbtnBox = (RadioButton) findViewById(R.id.box);
        rbtnParcel = (RadioButton) findViewById(R.id.parcel);
        rbtnParcel.setOnCheckedChangeListener(this);
        rbtnBox.setOnCheckedChangeListener(this);
        inputCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                String code = textView.getText().toString();
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if (rbtnParcel.isChecked()) {
                        ScanParcel(code);
                        inputCode.setText("");
                    }

                    if (rbtnBox.isChecked()) {
                        ScanBox(code);
                        inputCode.setText("");
                    }
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 将search 的结果组成一个list
     * 每次扫描都验证parcel是否已经在搜索的结果中；
     * 如果不是就dialog提醒
     */
    private void getSearchPkgs() {
        searchResult = (ArrayList<TSubPackage>) getIntent().getSerializableExtra("packages");
        if(searchResult == null){
            searchResult = PickingActivity.subPackagesScan;
        }
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
        String code = rawResult.getText();
        if (rbtnParcel.isChecked()) {
            ScanParcel(code);
            restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
            return;
        }
        if (rbtnBox.isChecked()) {
            ScanBox(code);
            restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
        }
    }

    protected void ScanBox(String code) {
        boxNo = splitBoxNo(code);
        tvBoxNum.setText("BoxNo:  " + boxNo);
        parcels.clear();
        tvParcelNumbs.setText("");
        rbtnParcel.setChecked(true);
    }

    protected void ScanParcel(String code) {
        if (parcels.contains(code)) {
            Toast.makeText(this, "Has scanned the parcel", Toast.LENGTH_SHORT).show();

        } else {
            boolean hasParcel = false;
            int size = searchResult.size();
            for (int i = 0; i < size; i++) {
                if (code.equals(searchResult.get(i).parcelNum)) {
                    hasParcel = true;
                }
            }
            if (hasParcel) {
                parcels.add(code);
                tvParcelSetText(parcels.toString());
                restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
            } else {//不在搜索列表中；
                showParcelErrorDialog(code);
            }
            Log.d("parcels", parcels.toString());
        }

    }

    public void showParcelErrorDialog(String code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(code + getString(R.string.is_not_in_search_list))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
                    }
                }).show();
    }

    private void tvParcelSetText(String string) {
        tvParcelNumbs.setText("parcelNo:  " + string);
    }

    private void putParcelToBox() {
        Intent i = new Intent();
        i.putExtra("box", boxNo);
        i.putExtra("parcels", parcels);
        setResult(RESULT_OK, i);
    }

    public void onConfirm(View view) {
        putParcelToBox();
        finish();
    }

    /**
     * 如果有分号的话截取分号的第一段；
     * 没有分号传全部
     *
     * @param code
     */
    public String splitBoxNo(String code) {
        if (TextUtils.isEmpty(code)) return "";
        if (!code.contains(";")) {
            return code;
        }
        String[] result = code.split(";");
        if (result.length >= 1) {
            code = result[0];
        }
        return code;

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.box:
                if (isChecked) {
                    inputCode.setText("");
                    tvBoxNum.setText("");
                    sb.delete(0, sb.length());//清空parcelnum
                    parcels.clear();
                    tvParcelNumbs.setText(sb);
                }
                break;
            case R.id.parcel:
                inputCode.setText("");
                break;
        }
    }


}
