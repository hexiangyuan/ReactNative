package com.daigou.selfstation.d2d;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.R;
import com.daigou.selfstation.activity.EzBaseActivity;
import com.daigou.selfstation.rpc.vehicle.TQueueNo;
import com.daigou.selfstation.rpc.vehicle.VehicleService;
import com.daigou.selfstation.utils.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Creator:HeXiangYuan
 * Date  : 16-11-23
 */

public class DriverQueueActivity extends EzBaseActivity implements View.OnClickListener {
    private TextView tvNum, btnApply, status, bReady, pReady;
    private Button btnTime, btnDate;
    private static final int MSG = 1000;
    private ProgressDialog progressBar;
    private ArrayList<RpcRequest> requests = new ArrayList<>();
    private RpcRequest quequeReq;
    private int timePos = -1;
    private MediaPlayer mpSucceeded;
    private ArrayList<String> timeSlots;
    private android.os.Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_queue);
        btnApply = (TextView) findViewById(R.id.btnApply);
        status = (TextView) findViewById(R.id.status);
        btnTime = (Button) findViewById(R.id.chooseTime);
        btnDate = (Button) findViewById(R.id.chooseDate);
        btnDate.setText(simpleDateFormat.format(calendar.getTime()));
        btnDate.setOnClickListener(this);
        btnTime.setOnClickListener(this);
        bReady = (TextView) findViewById(R.id.bReady);
        pReady = (TextView) findViewById(R.id.pReady);
        btnApply.setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);
        tvNum = (TextView) findViewById(R.id.num);
        progressBar = new ProgressDialog(this);
        progressBar.setMessage(getString(R.string.waiting));
        mpSucceeded = MediaPlayer.create(this, R.raw.success);
        loadTimeSlots();
        UserQueueForLoading();
        handler = new android.os.Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG:
                        UserQueueForLoading();
                        break;
                }
            }
        };
    }

    private void updateQueue(TQueueNo response) {
        if (response != null) {
            if (response.isInQueue) {
                handler.sendEmptyMessageDelayed(MSG, 5000);
                applyEnable(false);
                btnTime.setText(response.timeSlot);
                tvNum.setVisibility(View.VISIBLE);
                tvNum.setText("Packing　No.:" + response.no);
                if (!TextUtils.isEmpty(response.cageNum)) {
                    tvNum.setText(tvNum.getText().toString() + "\nCage No.：" + response.cageNum);
                }
                if (response.isBReady) {
                    bReady.setVisibility(View.VISIBLE);
                    if (firstReady) {
                        mpSucceeded.start();
                        firstReady = false;
                    }
                } else {
                    bReady.setVisibility(View.GONE);
                }
                if (response.isPReady) {
                    if (firstReady) {
                        mpSucceeded.start();
                        firstReady = false;
                    }
                    pReady.setVisibility(View.VISIBLE);
                } else {
                    pReady.setVisibility(View.GONE);
                }
                if (response.isLoading && firstLoading) {
                    status.setText(R.string.button_ok);
                    firstLoading = false;
                    mpSucceeded.start();
                    showSucceeded();
                } else if (!response.isLoading) {
                    status.setText(R.string.waiting);
                    status.setVisibility(View.VISIBLE);
                }
            } else {
                ToastUtil.showToast("You are out in the queue!!");
                handler.removeMessages(MSG);
            }
        } else {
            handler.sendEmptyMessageDelayed(MSG, 5000);
            ToastUtil.showToast(R.string.Network_is_error);
        }
    }

    private void loadTimeSlots() {
        RpcRequest request = VehicleService.UserGetTimeSlots(new Response.Listener<ArrayList<String>>() {
            @Override
            public void onResponse(ArrayList<String> response) {
                if (response != null) {
                    timeSlots = response;
                }
            }
        });
        requests.add(request);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnApply:
                if (TextUtils.isEmpty(btnTime.getText().toString())) {
                    ToastUtil.showToast(R.string.please_choose_time);
                    return;
                }
                ApplyLoading();
                break;
            case R.id.btnCancel:
                finishedLoading();
                break;
            case R.id.chooseTime:
                chooseTime();
                break;
            case R.id.chooseDate:
                chooseDate();
                break;
        }
    }

    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyMMdd");

    private void chooseDate() {
        final String calendarTemp = simpleDateFormat.format(calendar.getTime());
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.Dialog_DatePicker,
                // 绑定监听器
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        btnDate.setText(simpleDateFormat.format(calendar.getTime()));
                        btnTime.setText("");
                        if (!calendarTemp.equals(simpleDateFormat.format(calendar.getTime()))) {
                            status.setVisibility(View.GONE);
                            tvNum.setVisibility(View.GONE);
                            pReady.setVisibility(View.GONE);
                            pReady.setVisibility(View.GONE);
                            applyEnable(true);
                            UserQueueForLoading();
                        }
                    }
                }
                // 设置初始日期
                , calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar
                .get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    public void UserQueueForLoading() {
        int date = Integer.parseInt(btnDate.getText().toString());
        RpcRequest request = VehicleService.UserGetQueueNo(date, new Response.Listener<TQueueNo>() {
            @Override
            public void onResponse(TQueueNo response) {
                updateQueue(response);

            }
        });
        requests.add(request);
    }

    private void chooseTime() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (timeSlots == null || timeSlots.size() == 0) {
            ToastUtil.showToast(R.string.please_choose_time);
            return;
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, timeSlots);
        builder.setSingleChoiceItems(adapter, timePos == -1 ? 0 : timePos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                btnTime.setText(timeSlots.get(which));
                timePos = which;
                dialog.dismiss();
            }
        }).show();
    }

    private void finishedLoading() {
        progressBar.show();
        int date = Integer.parseInt(btnDate.getText().toString());
        RpcRequest request = VehicleService.UserDoneLoading(date, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.dismiss();
                if (response != null) {
                    ToastUtil.showToast("Finished");
                    if (quequeReq != null) {
                        quequeReq.cancel();
                    }
                    tvNum.setText("");
                    status.setVisibility(View.GONE);
                    bReady.setVisibility(View.GONE);
                    pReady.setVisibility(View.GONE);
                    handler.removeMessages(MSG);
                    applyEnable(true);
                } else {
                    ToastUtil.showToast(R.string.Network_is_error);
                }
            }
        });
        requests.add(request);
    }

    private void ApplyLoading() {
        progressBar.show();
        int date = Integer.parseInt(simpleDateFormat.format(calendar.getTime()));
        quequeReq = VehicleService.UserQueueForLoading(
                btnTime.getText().toString().trim(),
                date,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (null != response) {
                    if ("".equals(response)) {
                        status.setVisibility(View.VISIBLE);
                        status.setText(R.string.waiting);
                        ToastUtil.showToast("Apply succeed");
                        applyEnable(false);
                    } else {
                        ToastUtil.showToast(response);
                    }
                } else {
                    ToastUtil.showToast(R.string.Network_is_error);
                }
                progressBar.dismiss();
            }
        });
    }

    boolean firstLoading = true;
    boolean firstReady = true;

    private void showSucceeded() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("排队成功")
                .setMessage("老司机，到你了，Come On!")
                .setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        for (RpcRequest request : requests) {
            request.cancel();
        }
        if (quequeReq != null) {
            quequeReq.cancel();
        }
        handler.removeMessages(MSG);
        super.onDestroy();
    }


    private void applyEnable(boolean enable) {
        btnApply.setEnabled(enable);
        btnApply.setBackgroundColor(ContextCompat.getColor(this, enable ? R.color.colorAccent : R.color.gray));
    }
}
