package com.daigou.selfstation.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.R;
import com.daigou.selfstation.pick.presenter.PickPresenterImpl;
import com.daigou.selfstation.pick.ui.PickAdapter;
import com.daigou.selfstation.pick.ui.PickingActivity;
import com.daigou.selfstation.pick.view.BasePickActivity;
import com.daigou.selfstation.pick.view.IPickView;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.rpc.selfstation.TPickingJobDetail;
import com.daigou.selfstation.rpc.selfstation.TPickingSubPkgFilter;
import com.daigou.selfstation.rpc.selfstation.TSaveResult;
import com.daigou.selfstation.rpc.selfstation.TSaveResultMsg;
import com.daigou.selfstation.rpc.selfstation.TSaveSubPkgInfo;
import com.daigou.selfstation.rpc.selfstation.TSubPackage;
import com.daigou.selfstation.utils.CountryInfo;
import com.daigou.selfstation.utils.ToastUtil;
import com.pgyersdk.update.PgyUpdateManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StartPickingActivity extends BasePickActivity implements IPickView, View.OnClickListener {
    RecyclerView recyclerView;
    PickAdapter adapter;
    PickPresenterImpl presenter;
    String jobId;
    TextView beginTime, endTime;
    TextView deliveryMethod;//名称
    TextView tvColorShow;//颜色显示
    CheckBox checkBox;//已经拣货的过滤器
    TextView bp;//bp数量
    TextView surplus;//剩下的包裹数量
    ProgressDialog progressDialog;
    List<RpcRequest> requestList = new ArrayList<>();
    ArrayList<TSubPackage> subPkgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_picking);
        findViewById();
        jobId = getIntent().getStringExtra("jobId");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        presenter = new PickPresenterImpl(this);
        adapter = new PickAdapter(this, presenter);
        recyclerView.setAdapter(adapter);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        loadData();
    }

    public void findViewById() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.loading));
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        findViewById(R.id.tab_nick_name).setOnClickListener(this);
        findViewById(R.id.tab_shelf_num).setOnClickListener(this);
        findViewById(R.id.station).setOnClickListener(this);
        findViewById(R.id.colorLayout).setOnClickListener(this);
        beginTime = (TextView) findViewById(R.id.beginTime);
        tvColorShow = (TextView) findViewById(R.id.color);
        endTime = (TextView) findViewById(R.id.endTime);
        deliveryMethod = (TextView) findViewById(R.id.deliveryMethod);
        bp = (TextView) findViewById(R.id.bp);
        surplus = (TextView) findViewById(R.id.surplus);
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adapter.setPicked(isChecked);
                loadData();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.scan:
                if (subPkgs != null) {
                    Intent i = new Intent(this, ScanParcel2BoxActivity.class);
                    i.putExtra("packages", subPkgs);//subPackages
                    startActivityForResult(i, ScanParcel2BoxActivity.REQUEST_CODE);
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ScanParcel2BoxActivity.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String box = data.getStringExtra("box");
            ArrayList<String> parcels = data.getStringArrayListExtra("parcels");
            if (!TextUtils.isEmpty(box) && parcels.size() > 0) {
                saveParcels(box, parcels);
                adapter.scanResult(box, parcels);
            }
        }
    }

    /**
     * 上传所有扫描数据并且上传保存Parcel 到 box
     *
     * @param box
     * @param parcels
     */
    private void saveParcels(String box, ArrayList<String> parcels) {
        //上传保存扫描的box 和Parcel
        Map<String, String> bpMap = adapter.getBPMap();
        ArrayList<TSaveSubPkgInfo> pkgInfos = new ArrayList<>();
        for (String parcel : parcels) {
            TSaveSubPkgInfo tSaveSubPkgInfo = new TSaveSubPkgInfo();
            //如果这个货架号的字符包含B，就判断这个二级包裹号是B，如果不是，就判断为P，在扫描装箱的提交的时候自动保存。
            //先从bpMap里面找，没有就从box标记里面取
            String bp;
            if (bpMap.containsKey(parcel)) {
                bp = bpMap.get(parcel);
            } else {
                bp = "";
            }
            tSaveSubPkgInfo.BP = bp;
            tSaveSubPkgInfo.pkgId = PickingActivity.getPkgIdBySubPkg(subPkgs, parcel);
            tSaveSubPkgInfo.shipmentId = PickingActivity.getShipmentIdBySubPkgNo(subPkgs, parcel);
            tSaveSubPkgInfo.subPkgNum = parcel;
            pkgInfos.add(tSaveSubPkgInfo);
        }
        presenter.saveSubPkgs(box, pkgInfos);
    }

    @Override
    public void showProgressBar() {
        progressDialog.show();
    }

    @Override
    public void hideProgressBar() {
        progressDialog.dismiss();
    }

    @Override
    public void showError() {
        ToastUtil.showToast(R.string.Network_is_error);
    }

    @Override
    public void showSavedMsg(TSaveResult response) {
        if (response.isSucceeded) {
            Toast.makeText(this, R.string.all_are_saved, Toast.LENGTH_SHORT).show();
            loadData();
        } else {
            ArrayList<TSaveResultMsg> msgs = response.msgs;
            if (msgs == null || msgs.size() <= 0) return;
            ArrayList<String> strings = new ArrayList<>();
            for (TSaveResultMsg msg : msgs) {
                strings.add(msg.userName + ":" + msg.subPkgNum + ":" + msg.faildMsg);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, strings);
            builder.setAdapter(adapter, null);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    loadData();
                }
            });
            builder.show();
        }
    }

    private void loadData() {
        progressDialog.show();
        TPickingSubPkgFilter filter = new TPickingSubPkgFilter();
        filter.isPicked = checkBox.isChecked();
        requestList.add(DeliveryService.UserGetPickingJobDetail(jobId, filter,
                new Response.Listener<TPickingJobDetail>() {
                    public void onResponse(TPickingJobDetail response) {
                        progressDialog.dismiss();
                        if (response != null) {
                            subPkgs = response.subPkgs;
                            deliveryMethod.setText(response.deliveryPeriod + "-" + response.deliveryMethod + "-" + response.stationOrDriver);
                            String bpValue = ((response.bTodo + response.bDone) == 0 ? "" : response.bTodo + response.bDone + "B")
                                    + ((response.pTodo + response.pDone == 0 ? "" : response.pTodo + response.pDone + "P"));
                            deliveryMethod.setText(response.deliveryPeriod + "-" + response.deliveryMethod + "-" + response.stationOrDriver);
                            bp.setText("全部数量:" + bpValue);
                            surplus.setText("剩余数量:" + (response.bTodo == 0 ? "" : response.bTodo + "B") + (response.pTodo == 0 ? "" : response.pTodo + "P"));
                            beginTime.setText("开始时间:" + new SimpleDateFormat("MM/dd/yyyy").format(new Date(response.startTime * 1000)));
                            endTime.setText("结束时间:" + response.etcHour + "/" + response.etcMinute);
                            for (int i = 0; i < response.subPkgs.size(); i++) {
                                response.subPkgs.get(i).boxNums = response.boxNums;
                            }
                            adapter.setListData(response.subPkgs);
                            adapter.notifyDataSetChanged();
                            sortByShelfNum();
                        } else {
                            ToastUtil.showToast(R.string.Network_is_error);
                        }
                    }
                }));
    }

    private void sortByShelfNum() {
        ArrayList<TSubPackage> subPackages = adapter.getListDate();
        if (subPackages == null || subPackages.size() <= 0) {
            return;
        }
        if (adapter == null) return;
        if (CountryInfo.isMalaysia()) {
            PickingActivity.ShelfComparator myComparator = new PickingActivity.ShelfComparator();
            Collections.sort(subPackages, myComparator);
        }
        if (CountryInfo.isSingapore()) {
            subPackages = PickingActivity.sortByShelfNum(subPackages);
        }
        adapter.changeSubPackagesDate(subPackages);
    }

    private void sortByNhStationTag() {
        ArrayList<TSubPackage> subPackages = adapter.getListDate();
        if (subPackages == null || subPackages.size() <= 0) {
            return;
        }
        if (adapter == null) return;
        PickingActivity.NHStationComparator myComparator = new PickingActivity.NHStationComparator();
        Collections.sort(subPackages, myComparator);
        adapter.changeSubPackagesDate(subPackages);
    }

    private void sortByNickName() {
        ArrayList<TSubPackage> subPackages = adapter.getListDate();
        if (subPackages == null || subPackages.size() <= 0) {
            return;
        }
        if (adapter == null) return;
        PickingActivity.NickNameComparator myComparator = new PickingActivity.NickNameComparator();
        Collections.sort(subPackages, myComparator);
        adapter.changeSubPackagesDate(subPackages);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_nick_name:
                sortByNickName();
                break;
            case R.id.tab_shelf_num:
                sortByShelfNum();
                break;
            case R.id.station:
                sortByNhStationTag();
                break;
            case R.id.colorLayout:
                if (subPkgs != null && subPkgs.size() > 0){
                    filterColor();
                }else{
                    ToastUtil.showLongToast(R.string.keep_list_not_empty);
                }
                break;
        }

    }

    private void filterColor() {
        final ArrayList<String> colorList = new ArrayList<>();
        colorList.add("all");
        colorList.add("none");
        colorList.add("red");
        colorList.add("green");
        colorList.add("blue");
        colorList.add("yellow");
        colorList.add("orange");
        //更改颜色，
        showColorPick(colorList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                adapter.changeSubPackagesDate(FilterByColor(colorList.get(which)));
                changeColor(colorList.get(which));
            }
        });
    }

    private void changeColor(String color) {
        switch (color) {
            case "red":
                tvColorShow.setBackgroundResource(android.R.color.holo_red_light);
                break;
            case "green":
                tvColorShow.setBackgroundResource(android.R.color.holo_green_light);
                break;
            case "blue":
                tvColorShow.setBackgroundResource(android.R.color.holo_blue_light);
                break;
            case "yellow":
                tvColorShow.setBackgroundResource(R.color.yellow);
                break;
            case "orange":
                tvColorShow.setBackgroundResource(R.color.orange);
                break;
            case "none":
                tvColorShow.setBackgroundResource(android.R.color.white);
                break;
            default:
                tvColorShow.setBackgroundResource(android.R.color.transparent);
                break;
        }
    }

    private ArrayList<TSubPackage> FilterByColor(String color) {
        if (color.equals("none")) {
            color = "";
        } else if (color.equals("all")) {
            return subPkgs;
        }
        ArrayList<TSubPackage> listDate = subPkgs;
        ArrayList<TSubPackage> colorDate = new ArrayList<>();
        if (listDate != null && listDate.size() > 0) {
            for (TSubPackage pkg : listDate) {
                if (color.equals(pkg.packageScanLabelColor)) {
                    colorDate.add(pkg);
                }
            }
        }
        return colorDate;
    }

    private void showColorPick(ArrayList<String> colorList, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, colorList);
        builder.setAdapter(adapter, listener);
        builder.show();
    }
}
