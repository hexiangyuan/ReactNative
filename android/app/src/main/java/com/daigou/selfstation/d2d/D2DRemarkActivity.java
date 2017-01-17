package com.daigou.selfstation.d2d;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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


public class D2DRemarkActivity extends EzBaseActivity {
    private static final String BUNDLE_JOB = "bundle_job";
    EditText etRemark;
    Button btnRemark;
    private TDeliveryJob job;
    private RpcRequest request;
    private RpcRequest rpcRequest;
    private TextView tvTitle;
    private ArrayList<TTemplate> templates;
    private int titlePos = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d2d_remark);
        job = (TDeliveryJob) getIntent().getExtras().getSerializable(BUNDLE_JOB);
        etRemark = (EditText) findViewById(R.id.et_remark);
        tvTitle = (TextView) findViewById(R.id.title);
        btnRemark = (Button) findViewById(R.id.btn_remark);
        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseTitle();
            }
        });
        btnRemark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRemark.setClickable(false);
                remark();
            }
        });
        loadRemarksTemplate();
    }

    private void showChooseTitle() {
        if (templates == null) return;
        ArrayList<String> strings = new ArrayList<>();
        for (TTemplate template : templates) {
            strings.add(template.title);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, strings);
        builder.setSingleChoiceItems(adapter, titlePos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                titlePos = which;
                tvTitle.setText(templates.get(which).title);
                etRemark.setText(templates.get(which).content);
                dialog.dismiss();
            }
        }).show();
    }

    private void loadRemarksTemplate() {
        rpcRequest = D2DService.UserGetReMarkTemplates(new Response.Listener<ArrayList<TTemplate>>() {
            @Override
            public void onResponse(ArrayList<TTemplate> response) {
                if (response != null) {
                    templates = response;
                } else {
                    ToastUtil.showToast(R.string.Network_is_error);
                }
            }
        });
    }

    public static Bundle setArguments(TDeliveryJob job) {
        Bundle data = new Bundle();
        data.putSerializable(BUNDLE_JOB, job);
        return data;
    }

    private void remark() {
        String s = etRemark.getText().toString();
        if (TextUtils.isEmpty(s)) {
            ToastUtil.showToast("please input remark!");
            btnRemark.setClickable(true);
        } else {
             request = D2DService.UserAddRemark(job.ID, s, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    btnRemark.setClickable(true);
                    if (response != null) {
                        ToastUtil.showToast(R.string.succeeded);
                    } else {
                        ToastUtil.showToast(R.string.Network_is_error);
                    }
                }
            });
        }
    }

    @Override
    protected void onStop() {
        if(request != null){
            request.cancel();
            request = null;
        }
        if (rpcRequest != null) rpcRequest.cancel();
        super.onStop();
    }
}
