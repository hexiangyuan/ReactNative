package com.daigou.selfstation.d2d;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.R;
import com.daigou.selfstation.activity.EzBaseActivity;
import com.daigou.selfstation.rpc.selfstation.D2DService;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.rpc.selfstation.TDeliveryJob;
import com.daigou.selfstation.system.QiNiuConfiguration;
import com.daigou.selfstation.utils.ToastUtil;
import com.daigou.selfstation.view.HandWriteView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by undownding on 16-7-18.
 */
public class D2DSignActivity extends EzBaseActivity {

    private static final String BUNDLE_JOB = "bundle_job";

    private HandWriteView drawingView;
    private Button btnClear;
    private Button btnAck;
    private RatingBar ratingBar;
    private TDeliveryJob job;

    private int[] location = new int[2];
    private int height;
    private int width;

    private ArrayList<RpcRequest> requests = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d2d_sign);
        drawingView = (HandWriteView) findViewById(R.id.drawing);

        btnClear = (Button) findViewById(R.id.btn_clear);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.clear();
            }
        });

        btnAck = (Button) findViewById(R.id.btn_acknowledge);
        btnAck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSign();
            }
        });

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        job = (TDeliveryJob) getIntent().getExtras().getSerializable(BUNDLE_JOB);
    }

    public static Bundle setArguments(TDeliveryJob job) {
        Bundle data = new Bundle();
        data.putSerializable(BUNDLE_JOB, job);
        return data;
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

    private void doSign() {
        final ProgressDialog dialog = ProgressDialog.show(
                this, getTitle(), getString(R.string.loading), true
        );
        DeliveryService.UserGetUploadToken(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    QiNiuConfiguration.setToken(response);
                    File imageFile = saveImage();
                    upLoadSignatureImage(imageFile);
                    RpcRequest request = D2DService.UserSignParcelReceived(job.ID, QiNiuConfiguration.getKey(), ratingBar.getNumStars(), new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response != null && response.equals("")) {
                                dialog.dismiss();
                                ToastUtil.showLongToast(R.string.succeeded);
                                finish();
                            } else {
                                dialog.dismiss();
                                onError();
                            }
                        }
                    });
                    requests.add(request);
                } else {
                    dialog.dismiss();
                    onError();
                }
            }
        });
    }

    private void onError() {
        ToastUtil.showLongToast(R.string.msg_error);
    }

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



    @Override
    protected void onStop() {
        for (RpcRequest request : requests) {
            request.cancel();
        }
        super.onStop();

    }
}
