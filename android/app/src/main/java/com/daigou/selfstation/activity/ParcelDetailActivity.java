package com.daigou.selfstation.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.daigou.selfstation.R;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.rpc.selfstation.TParcel;
import com.daigou.selfstation.rpc.selfstation.TParcelSection;
import com.daigou.selfstation.system.QiNiuConfiguration;
import com.daigou.selfstation.utils.DensityUtil;
import com.daigou.selfstation.view.HandWriteView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by DaZhuang on 15/10/10.
 */
public class ParcelDetailActivity extends AppCompatActivity {

    private TextView tvParcelNO, tvUserID, tvTelephone, tvStatus;
    private LinearLayout parcelDetail;
    private HandWriteView drawingView;
    private Button btnAcknowledge;
    private String parcelNumber;
    private TParcel parcel;
    private RatingBar ratingBar;
    private boolean isRatingBarChange = false;
    private ProgressDialog progressDialog;
    private Button btnClear;
    private int[] location = new int[2];
    private int height;
    private int width;
    private Intent intent;
    private static final String TAG = "ParcelDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcel_detail);
        parcelNumber = getIntent().getStringExtra("ParcelNumber");
        Toast.makeText(ParcelDetailActivity.this, parcelNumber, Toast.LENGTH_SHORT).show();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tvParcelNO = (TextView) findViewById(R.id.tv_parcel_num);
        tvUserID = (TextView) findViewById(R.id.tv_user_id);
        tvTelephone = (TextView) findViewById(R.id.tv_telephone);
        drawingView = (HandWriteView) findViewById(R.id.drawing);
        parcelDetail = (LinearLayout) findViewById(R.id.parcel_detail);
        btnAcknowledge = (Button) findViewById(R.id.btn_acknowledge);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        btnClear = (Button) findViewById(R.id.btn_clear);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.clear();
            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                isRatingBarChange = true;
            }
        });
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.waiting));
        progressDialog.setCancelable(true);
        btnAcknowledge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRatingBarChange) {
                    progressDialog.show();
                    uploadData();
                } else {
                    Toast.makeText(ParcelDetailActivity.this, R.string.pls_rating_service,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        getParcel();
        intent = new Intent("com.daigou.selfstation.activity.ParcelDetailActivity.acknowledge");
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawingView = (HandWriteView) findViewById(R.id.drawing);
    }

    /**
     * 提交签名及parcel评价数据
     */
    private void uploadData() {
        DeliveryService.UserGetUploadToken(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    QiNiuConfiguration.setToken(response);
                    File imageFile = saveImage();
                    upLoadSignatureImage(imageFile);
                    DeliveryService.UserSetParcelReceived(parcelNumber,
                            QiNiuConfiguration.getKey(), ratingBar.getNumStars(),
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (response != null) {
                                        if (response.equals("")) {
                                            Toast.makeText(ParcelDetailActivity.this, R.string.thank_u,
                                                    Toast.LENGTH_LONG).show();
                                            sendBroadcast(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(ParcelDetailActivity.this, response,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    progressDialog.dismiss();
                                }
                            });
                }
            }
        });
    }

    // 目前根据parcelName不能获取Respone数据
    private void getParcel() {
        DeliveryService.UserGetParcel(parcelNumber, new Response.Listener<TParcel>() {
            public void onResponse(TParcel response) {
                if (response != null) {
                    progressDialog.dismiss();
                    parcel = response;
                    tvParcelNO.setText(getString(R.string.parcel_NO) + parcel.parcelNumber);
                    tvUserID.setText(getString(R.string.user_ID) + parcel.userName);
                    tvTelephone.setText(getString(R.string.phone_) + parcel.phone);
                    tvStatus.setText(getString(R.string.status_) + parcel.status);
                    for (TParcelSection tParcelSection : parcel.sections) {
                        TextView textView = new TextView(ParcelDetailActivity.this);
                        textView.setHeight(DensityUtil.dp2px(ParcelDetailActivity.this, 36));
                        textView.setGravity(Gravity.CENTER_VERTICAL);
                        textView.setText(tParcelSection.name + ": " + tParcelSection.value);
                        textView.setTextColor(Color.BLACK);
                        LinearLayout.LayoutParams layoutParams =
                                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT);
                        textView.setLayoutParams(layoutParams);
                        parcelDetail.addView(textView);
                    }
                } else {
                    Toast.makeText(ParcelDetailActivity.this, R.string.parcel_no_erro, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        drawingView.getLocationInWindow(location);
        height = drawingView.getHeight();
        width = drawingView.getWidth();
        Log.e("move", ev.getX() + " " + ev.getY());
        if (ev.getX() >= location[0] && ev.getX() <= location[0] + width &&
                ev.getY() >= location[1] && ev.getY() <= location[1] + height) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawingView.touchStart(ev.getX() - location[0], ev.getY() - location[1]);
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawingView.touchMove(ev.getX() - location[0], ev.getY() - location[1]);
                    Log.e("Move1", ev.getX() + " " + ev.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    Log.e("Move1", ev.getX() + " " + ev.getY());
                    drawingView.touchUp();
                    break;
            }
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

    /**
     * 保存图片
     *
     * @return
     */
    public File saveImage() {
        drawingView.setDrawingCacheEnabled(true);
        Bitmap bm = drawingView.getDrawingCache();
        File fPath = getCacheDir();
        File f;
        f = new File(fPath, UUID.randomUUID().toString() + ".png");
        try {
            FileOutputStream strm = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 80, strm);
            strm.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private void upLoadSignatureImage(File file) {
        QiNiuConfiguration.upload(file);
    }
}
