package com.daigou.selfstation.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.MyReactActivity;
import com.daigou.selfstation.R;
import com.daigou.selfstation.d2d.D2dListActivity;
import com.daigou.selfstation.d2d.DriverQueueActivity;
import com.daigou.selfstation.model.Competence;
import com.daigou.selfstation.nhpicking.NHPickingActivity;
import com.daigou.selfstation.pick.ui.PickingActivity;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.scan.ClearanceScanClass;
import com.daigou.selfstation.system.AppUrl;
import com.daigou.selfstation.utils.DensityUtil;
import com.daigou.selfstation.utils.LoginManager;
import com.daigou.selfstation.utils.PgyManager;
import com.daigou.selfstation.utils.ToastUtil;
import com.daigou.selfstation.view.EzbuyGridLayout;
import com.daigou.selfstation.webview.WebViewActivity;
import com.pgyersdk.update.PgyUpdateManager;

import java.util.ArrayList;
import java.util.List;

import static com.daigou.selfstation.model.Competence.DATA;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static int COLUMN_COUNT = 3;
    private EzbuyGridLayout gridLayout;
    private ProgressDialog progressDialog;
    private List<RpcRequest> requestList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        Competence.setData();
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundResource(android.R.color.white);
        scrollView.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setUserType();
        gridLayout = new EzbuyGridLayout(this);
        gridLayout.setRowAndColumn(COLUMN_COUNT, DATA.size());
        gridLayout.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addTextView();
        scrollView.addView(gridLayout);
        setContentView(scrollView);
        requestCameraPermissions();
        PgyManager.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PgyUpdateManager.unregister();
        for (int i = 0; i < requestList.size(); i++) {
            RpcRequest request = requestList.get(i);
            if (request != null) {
                request.cancel();
            }
        }
        requestList = null;
    }

    //不同的角色,权限不同
    private void setUserType() {
    }

    private void addTextView() {
        for (int i = 0; i < DATA.size(); i++) {
            TextView textView = new TextView(this);
            int padding = DensityUtil.dp2px(this, 14);
            textView.setPadding(padding, padding, padding, padding);
            textView.setId(DATA.get(i).getDesc());
            textView.setText(DATA.get(i).getDesc());
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(this);
            Drawable drawable = getResources().getDrawable(DATA.get(i).getDrawableId());
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            textView.setCompoundDrawables(null, drawable, null, null);
            int[] rowAndColumn = getRowAndColumn(i, COLUMN_COUNT);
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(GridLayout.spec(rowAndColumn[0], 1),
                    GridLayout.spec(rowAndColumn[1], 1));
            layoutParams.width = getWindowManager().getDefaultDisplay().getWidth() / COLUMN_COUNT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.setGravity(Gravity.FILL);
            textView.setLayoutParams(layoutParams);
            gridLayout.addView(textView);
        }
    }

    private static int[] getRowAndColumn(int index, int columnCount) {
        int row, column;
        row = index / columnCount;
        column = (index - (row * columnCount)) % columnCount;
        return new int[] { row, column };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.string.scan_to_shelf:
            Intent scanner = new Intent(this, ScanActivity.class);
            scanner.putExtra("from", ScanActivity.SCAN_TO_SHELF);
            startActivity(scanner);
            break;
        case R.string.search_parcel:
            Intent searchIt = new Intent(this, SearchParcelActivity.class);
            startActivity(searchIt);
            break;
        case R.string.scan_search:
            Intent ssi = new Intent(this, SearchSubParcelActivity.class);
            startActivity(ssi);
            break;
        case R.string.my_setting:
            Intent settingIt = new Intent(this, MySettingActivity.class);
            startActivity(settingIt);
            break;
        case R.string.parcel_list:
            Intent parcelListIntent = new Intent(this, ParcelListActivity.class);
            startActivity(parcelListIntent);
            break;
        case R.string.picking:
            startActivity(new Intent(this, PickingActivity.class));
            break;
        case R.string.out_for_delivery:
            Intent i = new Intent(this, ScanActivity.class);
            i.putExtra("from", ScanActivity.OUT_4_DELIVERY);
            startActivity(i);
            break;
        case R.string.ready_for_collection:
            Intent ready4Collection = new Intent(this, ScanActivity.class);
            ready4Collection.putExtra("from", ScanActivity.READY_FOR_COLLECTION);
            startActivity(ready4Collection);
            break;
        case R.string.sign:
            startActivity(new Intent(this, SignScanActivity.class));
            break;
        case R.string.partner_shop:
            Intent web = new Intent(this, WebViewActivity.class);
            web.putExtras(WebViewActivity.setArguments(AppUrl.getPartnerShopUrl()));
            startActivity(web);
            break;
        case R.string.d2d:
            Intent d2d = new Intent(this, D2dListActivity.class);
            startActivity(d2d);
            break;
        case R.string.neighborhood_picking:
            Intent nhPicking = new Intent(this, NHPickingActivity.class);
            startActivity(nhPicking);
            break;
        case R.string.check_in:
            checkIn();
            break;
        case R.string.packing:
            Intent packing = new Intent(this, DriverQueueActivity.class);
            startActivity(packing);
            break;
        case R.string.my_job:
            Intent myJobIntent = new Intent(this, MyJobActivity.class);
            startActivity(myJobIntent);
            break;
        case R.string.update:
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.ezbuy.ezbiz");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            break;
        case R.string.logout:
            new AlertDialog.Builder(this).setTitle(R.string.logout).setMessage(R.string.login_now)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            LoginActivity.deleteLoginInfo(MainActivity.this);
                            Intent intent = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    }).show();
            break;
        case R.string.work_schedule:
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goo.gl/forms/v7rTt4gY3cWuIt5a2"));
            startActivity(browserIntent);
            break;
        case R.string.cancel_form:
            Intent browserIntent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goo.gl/forms/5qtD08TvgiETIgDz1"));
            startActivity(browserIntent1);
            break;
        case R.string.scan_for_clearance:
            startActivity(new Intent(this, ClearanceScanClass.class));
            break;
        case R.string.react_native:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Intent intt = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intt, 1234);
                }else{
                    Intent ii = new Intent(this, MyReactActivity.class);
                    ii.putExtra("data", LoginManager.getUserType());
                    startActivity(ii);
                }
            }else{
                startActivity(new Intent(this, MyReactActivity.class));
            }
            break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    void requestCameraPermissions() {
        // Android6.0以上才能动态获取权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION }, 1);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {

                }else{
                    startActivity(new Intent(this, MyReactActivity.class));
                }
            }
        }
    }

    private void checkIn() {
        progressDialog.show();
        requestList.add(DeliveryService.UserPickingCheckIn(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                if (response != null) {
                    if ("".equals(response)) {
                        ToastUtil.showToast(R.string.check_in_successful);
                    } else {
                        ToastUtil.showToast(response);
                    }
                } else {
                    ToastUtil.showToast(R.string.Network_is_error);
                }
            }
        }));
    }
}