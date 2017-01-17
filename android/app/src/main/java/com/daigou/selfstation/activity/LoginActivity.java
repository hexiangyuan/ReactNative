package com.daigou.selfstation.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.daigou.model.TRpc;
import com.daigou.selfstation.BuildConfig;
import com.daigou.selfstation.R;
import com.daigou.selfstation.model.Competence;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.rpc.selfstation.TLoginResult;
import com.daigou.selfstation.rpc.selfstation.TServer;
import com.daigou.selfstation.system.AppUrl;
import com.daigou.selfstation.system.EzDeliveryApplication;
import com.daigou.selfstation.utils.LoginManager;
import com.daigou.selfstation.utils.PgyManager;
import com.daigou.selfstation.utils.SharePreferenceUtils;
import com.pgyersdk.update.PgyUpdateManager;

import java.util.ArrayList;

/**
 * A login screen that offers login via user id/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private AutoCompleteTextView mUserIdView;
    private EditText mPasswordView;
    private Spinner spinner;
    private Button mEmailSignInButton;
    private String country = "";
    private RadioGroup radioGroup;
    private static final String MODE_Living = "Living";
    private static final String MODE_Testing = "Testing";
    private static final String SG_COUNTRY = "Singapore";
    private static final String MY_COUNTRY = "Malaysia";
    private static final String ID_COUNTRY = "Indonesia";
    private static final String TH_COUNTRY = "Thailand";
    private int currentSpinnerPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUserIdView = (AutoCompleteTextView) findViewById(R.id.user_id);
        populateAutoComplete();
        spinner = (Spinner) findViewById(R.id.server_url);
        radioGroup = (RadioGroup) findViewById(R.id.mode);
        getSpinnerPos();
        initRadioGroup();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.living) {
                    AppUrl.setIsLiving(true);
                    AppUrl.kJsonRpcCoreUrl = "http://pdt.65daigou.com/api/";
                    EzDeliveryApplication.getInstance().setWebApiUrl(AppUrl.kJsonRpcCoreUrl);
                    loadServerUrl(MODE_Living);
                } else {
                    AppUrl.setIsLiving(false);
                    AppUrl.kJsonRpcCoreUrl = "http://delivery.65emall.net/api/";
                    EzDeliveryApplication.getInstance().setWebApiUrl(AppUrl.kJsonRpcCoreUrl);
                    loadServerUrl(MODE_Testing);
                }
            }
        });
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                attemptLogin();
            }
        });
        if (PgyManager.isFirstUse()) {
            deleteLoginInfo(this);
        }
        if (resumeLogin() == null) {
            loadServerUrl(radioGroup.getCheckedRadioButtonId() == R.id.testing ? MODE_Testing : MODE_Living);
            pgyUpdate();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * 从sp中获取spinner的pos
     */
    private void getSpinnerPos() {
        currentSpinnerPos = SharePreferenceUtils.getInt(getApplicationContext(), "countryPos", 0);
    }

    /**
     * 将spinner pos存在 sp
     *
     * @param position
     */
    private void saveSpinnerPos(int position) {
        SharePreferenceUtils.putInt(getApplicationContext(), "countryPos", position);
    }

    /**
     * 从Sp中取出mode,来初始化
     */
    private void initRadioGroup() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        AppUrl.setIsLiving(sp.getBoolean("mode", true));
        if (AppUrl.isLiving()) {
            radioGroup.check(R.id.living);
        } else {
            radioGroup.check(R.id.testing);
        }
    }

    /**
     * 蒲公英自动下载
     */
    private void pgyUpdate() {
        PgyManager.register(this);
    }


    void loadServerUrl(String mode) {
        DeliveryService.GetServers(mode, new Response.Listener<ArrayList<TServer>>() {
            public void onResponse(final ArrayList<TServer> response) {
                if (response != null) {
                    String[] urls = new String[response.size()];
                    for (int i = 0; i < response.size(); i++) {
                        urls[i] = response.get(i).name;
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(LoginActivity.this, android.R.layout.simple_spinner_dropdown_item, urls);
                    spinner.setAdapter(adapter);
                    if (currentSpinnerPos < urls.length) {
                        spinner.setSelection(currentSpinnerPos);
                    }
                    spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            AppUrl.kJsonRpcCoreUrl = response.get(i).url;
                            EzDeliveryApplication.getInstance().setWebApiUrl(AppUrl.kJsonRpcCoreUrl);
                            country = response.get(i).name;
                            currentSpinnerPos = i;
                            saveSpinnerPos(currentSpinnerPos);
                        }

                        public void onNothingSelected(AdapterView<?> adapterView) {
                            Toast.makeText(LoginActivity.this, "Choose Server", Toast.LENGTH_LONG).show();
                        }
                    });
                    AppUrl.kJsonRpcCoreUrl = response.get(currentSpinnerPos).url;
                    TRpc.getInstance().setWebApiUrl(AppUrl.kJsonRpcCoreUrl);
                }
            }
        });
    }


    /**
     * 自动填充帐号
     */
    private void populateAutoComplete() {
        // TODO: 15/10/10
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private static String cataLogCode;
    private void attemptLogin() {
        mUserIdView.setError(null);
        mPasswordView.setError(null);

        final String userID = mUserIdView.getText().toString();
        final String password = mPasswordView.getText().toString();
        switch (country) {
            case SG_COUNTRY:
                cataLogCode = "SG";
                break;
            case MY_COUNTRY:
                cataLogCode = "MY";
                break;
            case ID_COUNTRY:
                cataLogCode = "ID";
                break;
            case TH_COUNTRY:
                cataLogCode = "TH";
                break;
            default:
                cataLogCode = "SG";
        }

        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(userID)) {
            mUserIdView.setError(getString(R.string.error_field_required));
            focusView = mUserIdView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            EzDeliveryApplication.getInstance().setWebApiUrl(AppUrl.kJsonRpcCoreUrl);
            DeliveryService.Login(userID, password, cataLogCode, new Response.Listener<TLoginResult>() {
                @Override
                public void onResponse(TLoginResult response) {
                    if (response != null && response.isSuccessful) {
                        EzDeliveryApplication.getInstance().setLoginResult(response);
                        saveLoginInfo(LoginActivity.this, userID, password, cataLogCode, AppUrl.kJsonRpcCoreUrl, response);
                        EzDeliveryApplication.getInstance().setCookie(response.token);
                        SharePreferenceUtils.putInt(EzDeliveryApplication.getInstance(), "versionCode", BuildConfig.VERSION_CODE);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public static void saveLoginInfo(Context context, String user, String pwd, String country, String serverUrl, TLoginResult tLoginResult) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("username", user);
        editor.putString("password", pwd);
        editor.putString("serverUrl", serverUrl);
        editor.putString("country", country);
        editor.putString("country_sign", cataLogCode);
        editor.putString("userType", tLoginResult.userType);
        editor.putString("token", tLoginResult.token);
        editor.putBoolean("mode", AppUrl.isLiving());
        editor.putInt("stationSize", tLoginResult.StationNames.size());
        for (int i = 0; i < tLoginResult.StationNames.size(); i++) {
            editor.putString("stationName_" + i, tLoginResult.StationNames.get(i));
        }
        editor.apply();
    }

    public static void deleteLoginInfo(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("username", "");
        editor.putString("password", "");
        editor.putString("token", "");
        editor.putString("country", "");
        editor.putString("userType", "");
        editor.putString("serverUrl", "");
        if (Competence.DATA != null) {
            Competence.DATA.clear();
        }
        editor.apply();
    }

    TLoginResult resumeLogin() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String username = sp.getString("username", "");
        String password = sp.getString("password", "");
        String serverUrl = sp.getString("serverUrl", "");
        String countryName = sp.getString("country", "singapore");
        String userType = sp.getString("userType", LoginManager.DELIVERY_STAFF);
        AppUrl.setIsLiving(sp.getBoolean("mode", true));
        String token = sp.getString("token", "");
        if ("".equals(username) || "".equals(password) || "".equals(serverUrl) || "".equals(token)) {
            return null;
        } else {
            int size = sp.getInt("stationSize", 0);
            TLoginResult tLoginResult = new TLoginResult();
            ArrayList<String> stationNames = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                stationNames.add(sp.getString("stationName_" + i, ""));
            }
            tLoginResult.StationNames = stationNames;
            tLoginResult.token = token;
            mUserIdView.setText(username);
            mPasswordView.setText(password);
            country = countryName.equalsIgnoreCase(SG_COUNTRY) ? "SG" : "MY";
            tLoginResult.userType = userType;
            AppUrl.kJsonRpcCoreUrl = serverUrl;
            EzDeliveryApplication.getInstance().setCookie(token);
            EzDeliveryApplication.getInstance().setLoginResult(tLoginResult);
            return tLoginResult;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PgyUpdateManager.unregister();
    }
}