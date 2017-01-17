package com.daigou.selfstation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.daigou.selfstation.BuildConfig;
import com.daigou.selfstation.R;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.utils.LoginManager;

/**
 * Created by 65grouper on 15/10/9.
 */
public class MySettingActivity extends AppCompatActivity {
    private TextView tvUserId;
    private TextView tvAddress;
    private EditText etNewPassword;
    private EditText etRepeatPassword;
    private Button btnConfirm;
    private EditText etCurrentPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_setting);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initView();
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

    private void initView() {
        tvUserId = (TextView) findViewById(R.id.tv_user_id);
        tvUserId.setText(getString(R.string.prompt_user_id) + ":  " + PreferenceManager.getDefaultSharedPreferences(this).getString("username", ""));
        tvAddress = (TextView) findViewById(R.id.tv_address);
        btnConfirm = (Button) findViewById(R.id.tv_confirm);
        etNewPassword = (EditText) findViewById(R.id.et_new_password);
        etRepeatPassword = (EditText) findViewById(R.id.et_repeat_password);
        etCurrentPassword = (EditText) findViewById(R.id.et_current_password);
        tvAddress.setText(getString(R.string.user_type) + LoginManager.getUserType());
        ((TextView) (findViewById(R.id.version))).setText(getString(R.string.version) + BuildConfig.VERSION_NAME);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptConfirm();
            }
        });
    }

    private void attemptConfirm() {
        String newPassword = etNewPassword.getText().toString();
        String repeatPassword = etRepeatPassword.getText().toString();
        String currentPassword = etCurrentPassword.getText().toString();
        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError(getString(R.string.error_field_required));
            etCurrentPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError(getString(R.string.error_field_required));
            etNewPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(repeatPassword)) {
            etRepeatPassword.setError(getString(R.string.error_field_required));
            etRepeatPassword.requestFocus();
            return;
        }
        if (!newPassword.equals(repeatPassword)) {
            Toast.makeText(this, R.string.pwd_not_match, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentPassword.equals(newPassword)) {
            Toast.makeText(MySettingActivity.this, R.string.new_pwd_old, Toast.LENGTH_SHORT).show();
            return;
        }
        DeliveryService.UserModifyPassword(currentPassword, newPassword, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    if (response.equals("")) {
                        Toast.makeText(MySettingActivity.this, R.string.succeeded, Toast.LENGTH_SHORT).show();
                        //清除登陆信息
                        LoginActivity.deleteLoginInfo(getApplicationContext());
                        //重启App
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setClass(MySettingActivity.this, LoginActivity.class);
                        moveTaskToBack(true);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MySettingActivity.this, response, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MySettingActivity.this, getString(R.string.Network_is_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
