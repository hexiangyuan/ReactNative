package com.daigou.selfstation.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.R;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.utils.LoginManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.result.ResultHandler;

import java.util.ArrayList;

/**
 * Created by ：wangxiang on 15/10/13
 */
public class ScanActivity extends CaptureActivity {
    protected ArrayList<String> scanResult;
    protected EditText inputCode;//可能扫描不出,需要来手动输入
    protected TextView shelfTV;//货架编号
    protected String shelf;
    protected RadioButton shelfRB, parcelRB, pickUpRB;
    protected RadioGroup scan;
    protected android.support.v7.app.AlertDialog.Builder msgDialog;
    protected TextView record;
    public final static String READY_FOR_COLLECTION = "ReadyForCollection";
    public final static String SCAN_TO_SHELF = "ScanToShelf";
    public final static String OUT_4_DELIVERY = "Out4Delivery";
    private String from = "";
    protected MediaPlayer mpSucceeded, mpFailed;
    protected ProgressDialog progressDialog;
    protected ArrayList<RpcRequest> requests = new ArrayList<>();
    private static final int MSG_TIME = 1000;
    private Handler handler = new Handler() {//扫描货架号超过１０ｓ没有扫包裹要求重新扫描货架号
        @Override
        public void handleMessage(Message msg) {
            if (LoginManager.isPartTime()) {
                switch (msg.what) {
                    case MSG_TIME:
                        finish();
                        break;
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_layout_scan);
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mpSucceeded = MediaPlayer.create(this, R.raw.success);
        mpFailed = MediaPlayer.create(this, R.raw.lose);
        from = getIntent().getStringExtra("from");
        scanResult = new ArrayList<>();
        inputCode = (EditText) findViewById(R.id.input_code);
        shelfTV = (TextView) findViewById(R.id.shelf_id);
        scan = (RadioGroup) findViewById(R.id.scan);
        shelfRB = (RadioButton) findViewById(R.id.shelf_rb);
        record = (TextView) findViewById(R.id.record);
        record.setMovementMethod(ScrollingMovementMethod.getInstance());
        shelfRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    shelfTV.setText("");
                    statusView.setText("");
                    scanResult.clear();
                }
            }
        });
        parcelRB = (RadioButton) findViewById(R.id.parcel_rb);
        pickUpRB = (RadioButton) findViewById(R.id.pick_up_rb);
        pickUpRB.setVisibility(View.INVISIBLE);
        inputCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if (scan.getCheckedRadioButtonId() == R.id.shelf_rb) {
                        shelf = textView.getText().toString();
                        showShelf(shelf);
                        parcelRB.setChecked(true);
                        mpSucceeded.start();
                        inputCode.setText("");
                        return true;
                    }
                    if (scan.getCheckedRadioButtonId() == R.id.parcel_rb) {
                        putParcelToShelf(textView.getText().toString(), BarcodeFormat.QR_CODE);
                        inputCode.setText("");
                    }
                    if (scan.getCheckedRadioButtonId() == R.id.pick_up_rb) {
                        intentToSign(textView.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });
        msgDialog = new android.support.v7.app.AlertDialog.Builder(ScanActivity.this)
                .setTitle("Put Parcel")
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
                    }
                });
        changeRadioButtonName();
    }

    @Override
    protected void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
        super.handleDecodeInternally(rawResult, resultHandler, barcode);
        rawResult.getBarcodeFormat();
        putParcelToShelf(rawResult.getText(), rawResult.getBarcodeFormat());
    }


    void putParcelToShelf(String code, BarcodeFormat format) {
        if (scan.getCheckedRadioButtonId() == R.id.shelf_rb) {
            scanShelf(code);
        } else if (scan.getCheckedRadioButtonId() == R.id.parcel_rb) {
            scanParcel(code);
        } else if (scan.getCheckedRadioButtonId() == R.id.pick_up_rb) {
            mpSucceeded.start();
            intentToSign(code);
        }
    }

    private void scanParcel(String code) {
        if (shelf == null || code.equals(shelf)) {
            shelfRB.setChecked(true);
            Toast.makeText(this, "Please ic_scan shelf number", Toast.LENGTH_SHORT).show();
            restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
            mpFailed.start();
        } else if (scanResult.contains(code)) {
            Toast.makeText(this, "Has scanned the parcel", Toast.LENGTH_SHORT).show();
            restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
            mpFailed.start();
        } else {
            switch (from) {
                case READY_FOR_COLLECTION:
                    Log.d("ScanActivity", "Ready for Collection:" + code);
                    readyForCollection(code);
                    break;
                case SCAN_TO_SHELF:
                    Log.d("ScanActivity", "Scan to shelf:" + code);
                    handler.removeMessages(MSG_TIME);
                    handler.sendEmptyMessageDelayed(MSG_TIME, 10000);
                    scanToShelf(code);
                    break;
                case OUT_4_DELIVERY:
                    Log.d("ScanActivity", "out for delivery:" + splitBoxNo(code));
                    outForDelivery(splitBoxNo(code));
                    break;
            }
            showParcel(code);
        }
    }

    private void scanShelf(String code) {
        switch (from) {
            case SCAN_TO_SHELF:
                handler.sendEmptyMessageDelayed(MSG_TIME, 10000);
                break;
        }
        shelf = code;
        showShelf(code);
        parcelRB.setChecked(true);
        mpSucceeded.start();
        restartPreviewAfterDelay(2000);//2s后继续扫描
    }

    private void showParcel(String text) {
        switch (from) {
            case OUT_4_DELIVERY:
                statusView.setText("box/parcel:" + splitBoxNo(text));
                break;
            default:
                statusView.setText("parcel:" + text);
                break;
        }
    }

    private void showShelf(String code) {
        switch (from) {
            case OUT_4_DELIVERY:
                shelfTV.setText("cart:" + code);
                break;
            default:
                shelfTV.setText("shelf:" + code);
                break;
        }
    }

    private void intentToSign(String code) {
        Intent i = new Intent(this, ParcelDetailActivity.class);
        i.putExtra("ParcelNumber", code);
        startActivity(i);
        finish();
    }

    void scanToShelf(final String parcel) {
        showProgress();
        RpcRequest rpcRequest = DeliveryService.UserScanToShelf(shelf, parcel, new Response.Listener<String>() {
            public void onResponse(String response) {
                hideProgress();
                if (response == null) {
                    msgDialog.setMessage("Sorry server is busy please try again later");
                    msgDialog.show();
                    mpFailed.start();
                } else if ("".equals(response)) {//当且仅当response是""才是正常
                    mpSucceeded.start();
                    scanResult.add(parcel);
                    StringBuilder sb = new StringBuilder();
                    for (int i = scanResult.size() - 1; i >= 0; i--) {
                        sb.append(scanResult.get(i)).append('\n');
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    record.setText(sb.toString());
                    restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
                } else {
                    mpFailed.start();
                    msgDialog.setMessage(response).show();
                }
            }
        });
        requests.add(rpcRequest);
    }

    void readyForCollection(final String parcel) {
        showProgress();
        RpcRequest rpcRequest = DeliveryService.UserReadyForCollection(shelf, parcel, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgress();
                if (response == null) {
                    mpFailed.start();
                    msgDialog.setMessage("Sorry server is busy please try again later");
                    msgDialog.show();
                } else if ("".equals(response)) {//当且仅当response是""才是正常
                    scanResult.add(parcel);
                    StringBuilder sb = new StringBuilder();
                    for (int i = scanResult.size() - 1; i >= 0; i--) {
                        sb.append(scanResult.get(i)).append('\n');
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    record.setText(sb.toString());
                    mpSucceeded.start();
                    restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
                } else {
                    mpFailed.start();
                    msgDialog.setMessage(response).show();
                }
            }
        });
        requests.add(rpcRequest);
    }

    /**
     * outForDelivery
     *
     * @param box
     */
    private void outForDelivery(final String box) {
        showProgress();
        RpcRequest rpcRequest = DeliveryService.UserSetBoxOutForDelivery(shelf, box, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgress();
                if (response == null) {
                    mpFailed.start();
                    msgDialog.setMessage("Sorry server is busy please try again later");
                    msgDialog.show();
                } else if ("".equals(response)) {//当且仅当response是""才是正常
                    scanResult.add(box);
                    StringBuilder sb = new StringBuilder();
                    for (int i = scanResult.size() - 1; i >= 0; i--) {
                        sb.append(scanResult.get(i)).append('\n');
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    record.setText(sb.toString());
                    mpSucceeded.start();
                    restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
                } else {
                    mpFailed.start();
                    msgDialog.setMessage(response).show();
                }
            }
        });
        requests.add(rpcRequest);
    }

    void showProgress() {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    void hideProgress() {
        if (progressDialog.isShowing()) {
            progressDialog.hide();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeMessages(MSG_TIME);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        //cancel 所有的请求避免导致内存泄露；
        if (requests != null && requests.size() > 0) {
            for (RpcRequest request : requests) {
                request.cancel();
            }
        }
        super.onDestroy();
    }

    protected void changeRadioButtonName() {
        if (TextUtils.isEmpty(from)) return;
        switch (from) {
            case OUT_4_DELIVERY:
                shelfRB.setText(R.string.cart);
                parcelRB.setText(R.string.box_or_parcel);
                break;
            default:
                shelfRB.setText(R.string.shelf);
                parcelRB.setText(R.string.parcel);
                break;
        }
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
}