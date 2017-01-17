package com.daigou.selfstation.d2d;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.R;
import com.daigou.selfstation.activity.EzBaseActivity;
import com.daigou.selfstation.rpc.selfstation.D2DService;
import com.daigou.selfstation.rpc.selfstation.TDeliveryJob;
import com.daigou.selfstation.rpc.selfstation.TTemplate;
import com.daigou.selfstation.utils.ToastUtil;

import java.util.ArrayList;

/**
 * Created by undownding on 16-7-14.
 */
public class JobDetailActivity extends EzBaseActivity implements View.OnClickListener {

    private static final String BUNDLE_JOB = "bundle_job";

    private static final String STATUS_SIGNED = "signed";

    private TextView address;
    private TextView telephone;
    private TextView customerName;
    private TextView deliveryTime;
    private TextView postCode;
    private TextView smsStatus;
    private TextView status;
    private TextView memo;
    private TextView workRemark;
    private TextView eta;

    private Button btnSign;
    private Button btnRemark;
    private Button btnSms;
    private TDeliveryJob item;
    private String checkedTitle;
    private RpcRequest request;

    public static Bundle setArguments(TDeliveryJob job) {
        Bundle data = new Bundle();
        data.putSerializable(BUNDLE_JOB, job);
        return data;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);
        item = (TDeliveryJob) getIntent().getExtras().getSerializable(BUNDLE_JOB);
        address = (TextView) findViewById(R.id.tv_address);
        telephone = (TextView) findViewById(R.id.tv_telephone);
        customerName = (TextView) findViewById(R.id.tv_customer_name);
        deliveryTime = (TextView) findViewById(R.id.tv_delivery_time);
        postCode = (TextView) findViewById(R.id.tv_post_code);
        smsStatus = (TextView) findViewById(R.id.tv_sms_status);
        status = (TextView) findViewById(R.id.tv_status);
        memo = (TextView) findViewById(R.id.memo);
        workRemark= (TextView) findViewById(R.id.tv_work_remark);
        eta = (TextView) findViewById(R.id.tv_eta);
        btnSign = (Button) findViewById(R.id.sign_in);
        btnRemark = (Button) findViewById(R.id.btn_remark);
        btnSms = (Button) findViewById(R.id.send_sms);
        btnSign.setOnClickListener(this);
        btnRemark.setOnClickListener(this);
        btnSms.setOnClickListener(this);
        bind(item);
    }

    private void bind(final TDeliveryJob item) {
        address.setText("address:" + item.address);
        telephone.setText("tel:" + item.telephone);
        customerName.setText("name:" + item.customerName);
        deliveryTime.setText("date:" + item.deliveryDate);
        postCode.setText("post:" + item.postCode);
        smsStatus.setText("sms:" + item.smsStatus);
        status.setText("status:" + item.status);
        workRemark.setText("work remark:" + item.remark);
        eta.setText("eta:" + item.eta);
        StringBuilder builder = new StringBuilder();
        if (item.shipments != null && item.shipments.size() > 0) {
            memo.setVisibility(View.VISIBLE);
            int size = item.shipments.size();
            for (int i = 0; i < size; i++) {
                builder.append("memo:" + item.shipments.get(i).memo + "\n");
                if (!TextUtils.isEmpty(item.shipments.get(i).remark)) {
                    builder.append("user remark:" + item.shipments.get(i).remark + "\n");
                }
            }
            memo.setText(builder.toString());
        } else {
            memo.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in:
            if (STATUS_SIGNED.equals(item.status)) {
                ToastUtil.showToast(R.string.this_pkg_has_been_sign);
            } else {
                Intent intent = new Intent(JobDetailActivity.this, D2DSignActivity.class);
                intent.putExtras(D2DSignActivity.setArguments(item));
                startActivity(intent);
            }
            break;
            case R.id.btn_remark:
                Intent intent = new Intent(JobDetailActivity.this, D2DRemarkActivity.class);
                intent.putExtras(D2DRemarkActivity.setArguments(item));
                startActivity(intent);
                break;
            case R.id.send_sms:
                request = D2DService.UserGetSMSTemplates(new Response.Listener<ArrayList<TTemplate>>() {
                    @Override
                    public void onResponse(ArrayList<TTemplate> response) {
                        if (response != null) {
                            showSendSmsDialog(response);
                        } else {
                            ToastUtil.showToast(R.string.Network_is_error);
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    private void showSendSmsDialog(final ArrayList<TTemplate> response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = View.inflate(this, R.layout.dialog_layout_msg, null);
        final RadioGroup rgTitle = (RadioGroup) v.findViewById(R.id.rg_title);
        final EditText etContent = (EditText) v.findViewById(R.id.et_content);
        for (int i = 0; i < response.size(); i++) {
            RadioButton rbTitle = new RadioButton(this);
            rbTitle.setGravity(Gravity.CENTER);
            rbTitle.setButtonDrawable(null);
            rbTitle.setTextColor(ContextCompat.getColorStateList(this, R.color.selector_text_blue_black));
            rbTitle.setText(response.get(i).title);
            rbTitle.setId(i);
            rgTitle.addView(rbTitle, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        rgTitle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkedTitle = response.get(checkedId).title;
                etContent.setText(response.get(checkedId).content);
            }
        });
        builder.setView(v);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TTemplate tTemplate = new TTemplate();
                tTemplate.title = checkedTitle;
                tTemplate.content = etContent.getText().toString().trim();
                sendSms(tTemplate, item);
            }
        }).setNegativeButton(android.R.string.cancel, null).show();
    }

    private void sendSms(TTemplate tTemplate, TDeliveryJob job) {
        if (TextUtils.isEmpty(tTemplate.content)) {
            ToastUtil.showToast("Please input Sms content.");
            return;
        }
        ArrayList<String> jobs = new ArrayList<>();
        jobs.add(job.ID);
        D2DService.UserSendSmsMsg(jobs, tTemplate, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    if ("".equals(response))
                        ToastUtil.showToast("send succeed");
                } else {
                    ToastUtil.showToast("send failed");
                }
            }
        });
    }

    @Override
    protected void onStop() {
        if (request != null) request.cancel();
        super.onStop();
    }
}
