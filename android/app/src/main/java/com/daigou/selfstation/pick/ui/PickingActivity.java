package com.daigou.selfstation.pick.ui;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.daigou.selfstation.R;
import com.daigou.selfstation.activity.ScanParcel2BoxActivity;
import com.daigou.selfstation.pick.presenter.IPickPresenter;
import com.daigou.selfstation.pick.presenter.PickPresenterImpl;
import com.daigou.selfstation.pick.view.BasePickActivity;
import com.daigou.selfstation.rpc.selfstation.TCollectionStation;
import com.daigou.selfstation.rpc.selfstation.TDeliveryMethod;
import com.daigou.selfstation.rpc.selfstation.TFindSubPackageResult;
import com.daigou.selfstation.rpc.selfstation.TSaveResult;
import com.daigou.selfstation.rpc.selfstation.TSaveResultMsg;
import com.daigou.selfstation.rpc.selfstation.TSaveSubPkgInfo;
import com.daigou.selfstation.rpc.selfstation.TSubPackage;
import com.daigou.selfstation.utils.CountryInfo;
import com.daigou.selfstation.utils.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by 何祥源 on 16/4/27.
 * Desc: pick Activity
 */
public class PickingActivity extends BasePickActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private ArrayList<TDeliveryMethod> methods = new ArrayList<>();
    private ArrayList<String> selfCollections = new ArrayList<>();
    private ArrayList<String> neighborhoodRegion = new ArrayList<>();
    private ArrayList<TCollectionStation> collectionStations = new ArrayList<>();
    private ArrayList<TSubPackage> subPackages = new ArrayList<>();
    private int deliveryMethodPosition = 0;
    private int periodPosition = 0;
    private int driverPosition = 0;
    private int housePosition = 0;
    private int currentBPPos = 0;
    private int currentAMPMPos = 0;
    private PickAdapter listAdapter;
    private IPickPresenter presenter;
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;
    private Button btnDeliveryMethod, btnTimeStart, btnTimeEnd, btnDeliveryMen, btnWarehouses, btnPickupTime, btnLock, btnUnLock, btnSearch, btnBPNo, btnScan, btnAMPM;
    private TextView tvTabNickName, tvTabShelfNum;
    private CheckBox picked, checkAll;
    private String str;
    private ProgressDialog progressDialog;
    private String currentDeliveryCode = "";
    private static final String CODE_HOME = "Home";
    private static final String CODE_SUBWAY = "Subway";
    private static final String CODE_SELF_COLLECTION = "SelfCollection";
    private static final String CODE_NEIGHBOURHOOD_STATION = "NeighbourhoodStation";
    private static final String MCDONALD_STRING = "McDonald Self Collection";
    private static final String[] BP = {"All", "B", "P"};
    private static final String[] AMPM = {"All", "AM", "PM"};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_picking);
        presenter = new PickPresenterImpl(this);
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        initView();
        initToolbar();
        presenter.loadDeliveryMethod();
    }

    private void initView() {
        listAdapter = new PickAdapter(this, presenter);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(listAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();

        tvTabNickName = (TextView) findViewById(R.id.tab_nick_name);
        tvTabShelfNum = (TextView) findViewById(R.id.tab_shelf_num);
        tvTabNickName.setOnClickListener(this);
        tvTabShelfNum.setOnClickListener(this);
        findViewById(R.id.station).setOnClickListener(this);

        picked = (CheckBox) findViewById(R.id.cb_picked);
        checkAll = (CheckBox) findViewById(R.id.check_all);
        btnDeliveryMethod = (Button) findViewById(R.id.delivery_method);
        btnTimeStart = (Button) findViewById(R.id.time_start);
        btnTimeEnd = (Button) findViewById(R.id.time_end);
        btnDeliveryMen = (Button) findViewById(R.id.drivers);
        btnWarehouses = (Button) findViewById(R.id.warehouse);
        btnWarehouses.setHint(R.string.color);
        btnPickupTime = (Button) findViewById(R.id.pickup_time);
        btnAMPM = (Button) findViewById(R.id.btn_AM_PM);
        btnUnLock = (Button) findViewById(R.id.unlock);
        btnLock = (Button) findViewById(R.id.lock);
        btnSearch = (Button) findViewById(R.id.search);
        if (CountryInfo.isMalaysia()) {
            btnLock.setVisibility(View.INVISIBLE);
            btnUnLock.setVisibility(View.INVISIBLE);
        }
        btnBPNo = (Button) findViewById(R.id.bp);
        btnScan = (Button) findViewById(R.id.scan);
        btnDeliveryMethod.setOnClickListener(this);
        btnTimeStart.setOnClickListener(this);
        btnTimeEnd.setOnClickListener(this);
        btnDeliveryMen.setOnClickListener(this);
        btnWarehouses.setOnClickListener(this);
        btnPickupTime.setOnClickListener(this);
        btnLock.setOnClickListener(this);
        btnUnLock.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnBPNo.setOnClickListener(this);
        btnScan.setOnClickListener(this);
        btnAMPM.setOnClickListener(this);
        picked.setOnCheckedChangeListener(this);
        checkAll.setOnCheckedChangeListener(this);
        initTimeToday();
    }

    private void initTimeToday() {
        btnTimeStart.setText(simpleDateFormat.format(calendar.getTime()));
        btnTimeEnd.setText(simpleDateFormat.format(calendar.getTime()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delivery_method:
                presenter.deliveryMethodClicked();
                break;
            case R.id.time_start:
                presenter.startTimeClicked();
                break;
            case R.id.time_end:
                presenter.endTimeClicked();
                break;
            case R.id.drivers:
                driversCase();
                break;
            case R.id.warehouse:
//                presenter.showHouses();
                if (subPackages != null && subPackages.size() > 0){
                    filterColor();
                }else{
                    ToastUtil.showLongToast(R.string.keep_list_not_empty);
                }
                break;
            case R.id.pickup_time:
                pickTimeClicked();
                break;
            case R.id.unlock:
                if (getCheckedShipmentIds() != null) {
                    presenter.isLock(getCheckedShipmentIds(), false);
                }
                break;
            case R.id.search:
                search();
                set.clear();
                break;
            case R.id.bp:
                if (subPackages != null && subPackages.size() > 0) {
                    showBPSelect();
                } else {
                    ToastUtil.showLongToast(R.string.keep_list_not_empty);
                }
                break;
            case R.id.lock:
                if (getCheckedShipmentIds() != null) {
                    presenter.isLock(getCheckedShipmentIds(), true);
                }
                break;
            case R.id.scan:
                scanClick();
                break;
            case R.id.tab_nick_name://点击顶部的NickName 排序
                sortByNickName();
                break;
            case R.id.tab_shelf_num://点击顶部的shelfNum 排序
                sortByShelfNum();
                break;
            case R.id.btn_AM_PM:
                showAMPM();
                break;
            case R.id.station:
                showNHStationTags();
                sortByNhStationTag();
                break;
        }
    }

    private void showAMPM() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, AMPM);
        builder.setSingleChoiceItems(adapter, currentAMPMPos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentAMPMPos = which;
                btnAMPM.setText(AMPM[currentAMPMPos]);
                dialog.dismiss();
            }
        }).show();
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
                listAdapter.changeSubPackagesDate(FilterByColor(colorList.get(which)));
                changeColor(colorList.get(which));
            }
        });
    }

    private void showColorPick(ArrayList<String> colorList, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, colorList);
        builder.setAdapter(adapter, listener);
        builder.show();
    }

    private ArrayList<TSubPackage> FilterByColor(String color) {
        if (color.equals("none")) {
            color = "";
        } else if (color.equals("all")) {
            return subPackages;
        }
        ArrayList<TSubPackage> listDate = subPackages;
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

    private void changeColor(String color) {
        btnWarehouses.setText(color);
    }

    private void showBPSelect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, BP);
        builder.setSingleChoiceItems(adapter, currentBPPos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentBPPos = which;
                switch (currentBPPos) {
                    case 0:
                        listAdapter.changeSubPackagesDate(subPackages);
                        break;
                    case 1:
                        showBSubPackage();
                        break;
                    case 2:
                        showPSubPackage();
                        break;
                }
                dialog.dismiss();
            }
        }).show();
    }

    public static ArrayList<TSubPackage> subPackagesScan = new ArrayList<>();//intent大小有１M限制

    private void scanClick() {
        if (subPackages != null && subPackages.size() > 0) {
            subPackagesScan = subPackages;
                Intent i = new Intent(this, ScanParcel2BoxActivity.class);
                startActivityForResult(i, ScanParcel2BoxActivity.REQUEST_CODE);
        } else {
                ToastUtil.showLongToast(R.string.keep_list_not_empty);
        }
    }

    private void pickTimeClicked() {
        if (TextUtils.isEmpty(currentDeliveryCode)) {
            Toast.makeText(PickingActivity.this, R.string.deliveryMethod_must_be_chose, Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentDeliveryCode.contains(CODE_HOME)) {
            presenter.showAllPeriod("Home");
        } else if (CODE_SUBWAY.equalsIgnoreCase(currentDeliveryCode)) {

        } else if (CODE_SELF_COLLECTION.equalsIgnoreCase(currentDeliveryCode)) {
            presenter.showAllPeriod(btnDeliveryMen.getText().toString());
        } else if (CODE_NEIGHBOURHOOD_STATION.equalsIgnoreCase(currentDeliveryCode)) {
            presenter.showNeighborhoodStation(btnDeliveryMen.getText().toString());
        } else { //MY
            presenter.showAllPeriod(btnDeliveryMen.getText().toString());
        }
    }

    private void driversCase() {
        if (TextUtils.isEmpty(currentDeliveryCode)) {
            Toast.makeText(PickingActivity.this, R.string.deliveryMethod_must_be_chose, Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentDeliveryCode.contains(CODE_HOME)) {//包含Home 的delivery method 加载司机
            presenter.showAllDriver();
        } else {
            switch (currentDeliveryCode) {
                case CODE_SUBWAY:
                    presenter.showMRTStations(true);
                    break;
                case CODE_SELF_COLLECTION:
                    presenter.showSelfCollectionStations();
                    break;
                case CODE_NEIGHBOURHOOD_STATION:
                    presenter.userGetNeighborhoodRegions(true);
                    break;
                default:
                    for (int i = 0; i < methods.size(); i++) {
                        if (currentDeliveryCode.equals(methods.get(i).deliveryCode)) {
                            collectionStations = methods.get(i).collectionStations;
                        }
                    }
                    ArrayList<String> strings = stationsToList(collectionStations);
                    strings = addAllInList(strings, "All");
                    showArrayListDialog(strings, btnDeliveryMen, driverPosition);
                    break;
            }
        }
    }

    private void search() {
        if (TextUtils.isEmpty(currentDeliveryCode)) {
            Toast.makeText(PickingActivity.this, R.string.deliveryMethod_must_be_chose, Toast.LENGTH_SHORT).show();
            return;
        }
        String startTime = btnTimeStart.getText().toString();
        String startEnd = btnTimeEnd.getText().toString();
        boolean isPicked = picked.isChecked();
        // 如果字段中包含All就传“”,索引所有的；
        String deliveryMen = btnDeliveryMen.getText().toString().contains("All") ? "" : btnDeliveryMen.getText().toString();
        String period = btnPickupTime.getText().toString().contains("All") ? "" : btnPickupTime.getText().toString();
        btnWarehouses.setText("");
        int house = 0;
        //MY 非McDonald都没有时间筛选条件
        if (CountryInfo.isMalaysia() && !currentDeliveryCode.contains(MCDONALD_STRING)) {
            startTime = "";
            startEnd = "";
        }
        String AMPM = btnAMPM.getText().toString().contains("All") ? "" : btnAMPM.getText().toString();
        //如果有“：”就截取前面的字符
        if (currentDeliveryCode.equalsIgnoreCase(CODE_NEIGHBOURHOOD_STATION) && period.contains(":")) {
            int i = period.indexOf(":");
            period = period.substring(i + 1, period.length());
        }
        presenter.search(currentDeliveryCode, startTime, startEnd, isPicked, deliveryMen, period, house,AMPM);
    }

    private ArrayList<String> stationsToList(ArrayList<TCollectionStation> collectionStations) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < collectionStations.size(); i++) {
            list.add(collectionStations.get(i).stationName);
        }
        return list;
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showProgressBar() {
        progressDialog.show();
    }

    @Override
    public void hideProgressBar() {
        progressDialog.hide();
    }

    @Override
    public String showDateChooseDialog(final TextView textView) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.Dialog_DatePicker,
                // 绑定监听器
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        textView.setText(simpleDateFormat.format(calendar.getTime()));
                    }
                }
                // 设置初始日期
                , calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar
                .get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
        return null;
    }

    public String showArrayListDialog(final ArrayList<String> listStrings, final TextView textView, final int choosePosition) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, listStrings);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(adapter, choosePosition, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textView.setText(listStrings.get(which));
                str = listStrings.get(which);
                switch (textView.getId()) {
                    case R.id.drivers:
                        driverPosition = which;
                        btnPickupTime.setText("");
                        break;
                    case R.id.warehouse:
                        housePosition = which;
                        break;
                    case R.id.pickup_time:
                        periodPosition = which;
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.show();
        return str;
    }

    private void timeCanClick() {
        btnTimeEnd.setClickable(true);
        btnTimeStart.setClickable(true);
        initTimeToday();
    }

    @Override
    public void searchClicked(TFindSubPackageResult response) {
        if(response != null){
            btnBPNo.setText(response.totalBoxCount + "B" + response.totalPackageCount + "P");
            subPackages = response.subPackages;
            if (CountryInfo.isSingapore()) {//搜索后就排序！！！！！
                subPackages = sortByShelfNum(subPackages);
            }
            listAdapter.search(subPackages);
            checkAll.setChecked(false);
        }else{
            subPackages.clear();
            listAdapter.search(subPackages);
        }
    }

    @Override
    public void showError() {
        Toast.makeText(PickingActivity.this, R.string.Network_is_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void checkAll() {
        listAdapter.checkAll();
    }

    @Override
    public void clearCheck() {
        listAdapter.clearCheck();
    }

    @Override
    public void lockSucceeded(boolean isLock, ArrayList<Integer> Shipments) {
        if (isLock) {
            Toast.makeText(PickingActivity.this, R.string.lock_succeeded, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(PickingActivity.this, R.string.unlock_succeeded, Toast.LENGTH_SHORT).show();
        }
        listAdapter.changeLocked(isLock, Shipments);
    }

    @Override
    public void scan() {

    }

    @Override
    public void showPeriod(ArrayList<String> response) {
        showArrayListDialog(addAllInList(response, "All Period"), btnPickupTime, periodPosition);
    }

    @Override
    public void showRegion() {
        showArrayListDialog(addAllInList(neighborhoodRegion, "All Region"), btnDeliveryMen, driverPosition);
    }

    @Override
    public void showNeighborhoodStations(ArrayList<String> response) {
        showArrayListDialog(addAllInList(response, "All Station"), btnPickupTime, periodPosition);
    }

    @Override
    public void showDrivers(ArrayList<String> response) {
        showArrayListDialog(addAllInList(response, "All Driver"), btnDeliveryMen, driverPosition);

    }

    @Override
    public void showHouses(ArrayList<String> response) {
        showArrayListDialog(addAllInList(response, "All House"), btnWarehouses, housePosition);
    }

    @Override
    public void showMRTStation(ArrayList<String> response) {
        showArrayListDialog(addAllInList(response, "All MRT Station"), btnDeliveryMen, driverPosition);
    }

    @Override
    public void showSelfCollections() {
        showArrayListDialog(selfCollections, btnDeliveryMen, driverPosition);
    }

    @Override
    public void showDeliveryMethod() {
        if (methods == null || methods.size() <= 0) return;
        final ArrayList<String> strings = new ArrayList<>();
        for (TDeliveryMethod method : methods) {
            strings.add(method.deliveryName);
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, strings);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(adapter, deliveryMethodPosition, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                btnTimeEnd.setClickable(true);
                btnTimeStart.setClickable(true);
                deliveryMethodPosition = which;
                periodPosition = 0;
                driverPosition = 0;
                housePosition = 0;
                btnDeliveryMethod.setText(strings.get(which));
                currentDeliveryCode = methods.get(which).deliveryCode;
                /**
                 * 取货方式包含 “McDonald Self Collection”字样的才能安排取货时间
                 */
                if (CountryInfo.isMalaysia() && !currentDeliveryCode.contains(MCDONALD_STRING)) {
                    btnTimeStart.setText("");
                    btnTimeEnd.setClickable(false);
                    btnTimeEnd.setText("");
                    btnTimeStart.setClickable(false);
                } else {
                    timeCanClick();
                }
                if (currentDeliveryCode.contains(CODE_HOME)) {
                    btnDeliveryMen.setHint(R.string.all_driver);
                    btnPickupTime.setVisibility(View.VISIBLE);
                    btnPickupTime.setHint(R.string.all_period);
                } else if (CODE_SUBWAY.equalsIgnoreCase(currentDeliveryCode)) {
                    btnDeliveryMen.setHint(R.string.all_mrt_station);
                    btnPickupTime.setVisibility(View.INVISIBLE);
                } else if (CODE_SELF_COLLECTION.equalsIgnoreCase(currentDeliveryCode)) {
                    btnDeliveryMen.setHint(R.string.warehouse);
                    btnPickupTime.setVisibility(View.VISIBLE);
                    btnPickupTime.setHint(R.string.all_period);
                } else if (CODE_NEIGHBOURHOOD_STATION.equalsIgnoreCase(currentDeliveryCode)) {
                    btnDeliveryMen.setHint(R.string.neighborhood);
                    btnPickupTime.setVisibility(View.VISIBLE);
                    btnPickupTime.setHint(R.string.All_Station);
                } else {
                    btnDeliveryMen.setHint(R.string.All_Station);
                    btnPickupTime.setVisibility(View.VISIBLE);
                    btnPickupTime.setHint(R.string.all_period);
                }
                btnPickupTime.setText("");
                btnDeliveryMen.setText("");
                btnWarehouses.setText("");
                dialog.dismiss();
            }
        });
        builder.show();
    }


    @Override
    public void showStartTime() {
        showDateChooseDialog(btnTimeStart);
    }

    @Override
    public void showEndTime() {
        showDateChooseDialog(btnTimeEnd);
    }

    @Override
    public void isPickChecked() {

    }

    @Override
    public void methodLoadFinished(ArrayList<TDeliveryMethod> response) {
        if (response != null && response.size() >= 0) {
            for (TDeliveryMethod method : response) {
                methods.add(method);
                if (method.deliveryCode.contains(CODE_HOME)) {

                } else if (CODE_SELF_COLLECTION.equalsIgnoreCase(method.deliveryCode)) {
                    if (method.collectionStations != null && method.collectionStations.size() > 0) {
                        for (TCollectionStation station : method.collectionStations) {
                            selfCollections.add(station.stationName);
                        }
                        selfCollections = addAllInList(selfCollections, "All");
                    }
                }
            }
            btnDeliveryMethod.setText(methods.get(0).deliveryName);
            currentDeliveryCode = methods.get(0).deliveryCode;
        }
    }

    @Override
    public void saveClicked(String response) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.cb_picked) {
            if (listAdapter != null)
                listAdapter.setPicked(isChecked);
            if (isChecked)
                checkAll.setChecked(false);
        } else if (buttonView.getId() == R.id.check_all) {
            if (isChecked) {
                listAdapter.checkAll();
            } else {
                listAdapter.clearCheck();
            }
        }

    }

    public ArrayList<Integer> getCheckedShipmentIds() {
        ArrayList<TSubPackage> checkedParcels = listAdapter.getCheckedParcels();
        if (checkedParcels == null || checkedParcels.size() <= 0) return null;
        ArrayList<Integer> shipmentIds = new ArrayList<>();
        for (TSubPackage tSubPackage : checkedParcels) {
            shipmentIds.add(tSubPackage.shipmentId);
        }
        return shipmentIds;
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        presenter.cancelRpcRequests();
        subPackagesScan.clear();
        super.onDestroy();

    }

    /**
     * 在list中增加All 列表选项；
     *
     * @param list
     * @param str
     * @return
     */

    protected ArrayList<String> addAllInList(ArrayList<String> list, String str) {
        list.add(0, str);
        return list;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ScanParcel2BoxActivity.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String box = data.getStringExtra("box");
            ArrayList<String> parcels = data.getStringArrayListExtra("parcels");
            Log.i("box", parcels.toString());
            if (!TextUtils.isEmpty(box) && parcels.size() > 0) {
                saveParcels(box, parcels);
                listAdapter.scanResult(box, parcels);
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
        Map<String, String> bpMap = listAdapter.getBPMap();
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
            tSaveSubPkgInfo.pkgId = getPkgIdBySubPkg(subPackages,parcel);
            tSaveSubPkgInfo.shipmentId = getShipmentIdBySubPkgNo(subPackages,parcel);
            tSaveSubPkgInfo.subPkgNum = parcel;
            pkgInfos.add(tSaveSubPkgInfo);
        }
        presenter.saveSubPkgs(box, pkgInfos);
    }

    /**
     * 扫描之后保存完毕信息的处理；
     * 如果全部保存成功，列表中 remove 保存的数据
     * 如果有保存失败的就Dialog失败的信息，list展示失败的信息；
     *
     * @param result saveResult
     */
    @Override
    public void showSavedMsg(TSaveResult result) {
        if (result.isSucceeded) {
            Toast.makeText(this, R.string.all_are_saved, Toast.LENGTH_SHORT).show();
        } else {
            ArrayList<TSaveResultMsg> msgs = result.msgs;
            showSaveFailedDialog(msgs);
        }
        search();
    }

    @Override
    public void neighborhoodRegionsLoaded(ArrayList<String> response) {
        neighborhoodRegion = response;
        presenter.showNeighborRegion();
    }

    /**
     * save不成功的是展示dialog
     *
     * @param msgs
     */
    private void showSaveFailedDialog(ArrayList<TSaveResultMsg> msgs) {
        if (msgs == null || msgs.size() <= 0) return;
        ArrayList<String> strings = new ArrayList<>();
        for (TSaveResultMsg msg : msgs) {
            strings.add(msg.userName + ":" + msg.subPkgNum + ":" + msg.faildMsg);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, strings);
        builder.setAdapter(adapter, null);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    /**
     * 通过二级包裹号筛选出ShipmentId
     * Param:二级包裹号
     *
     * @retun ShipmentId
     */
    public static int getShipmentIdBySubPkgNo(ArrayList<TSubPackage> subPackages,String subPkg) {
        if (subPackages == null || subPackages.size() <= 0) return 0;
        for (TSubPackage subPackage : subPackages) {
            if (subPkg.equals(subPackage.parcelNum)) {
                return subPackage.shipmentId;
            }
        }
        return 0;
    }

    /**
     * 通过二级包裹号筛选出PkgId
     * Param:二级包裹号
     *
     * @retun PackageId
     */
    public static int getPkgIdBySubPkg(ArrayList<TSubPackage> subPackages,String subPkg) {
        if (subPackages == null || subPackages.size() <= 0) return 0;
        for (TSubPackage subPackage : subPackages) {
            if (subPkg.equals(subPackage.parcelNum)) {
                return subPackage.packageId;
            }
        }
        return 0;
    }

    private void sortByNickName() {
        ArrayList<TSubPackage> subPackages = listAdapter.getListDate();
        if (subPackages == null || subPackages.size() <= 0) {
            return;
        }
        if (listAdapter == null) return;
        NickNameComparator myComparator = new NickNameComparator();
        Collections.sort(subPackages, myComparator);
        listAdapter.changeSubPackagesDate(subPackages);
    }

    private void sortByShelfNum() {
        ArrayList<TSubPackage> subPackages = listAdapter.getListDate();
        if (subPackages == null || subPackages.size() <= 0) {
            return;
        }
        if (listAdapter == null) return;
        if (CountryInfo.isMalaysia()) {
            ShelfComparator myComparator = new ShelfComparator();
            Collections.sort(subPackages, myComparator);
        }
        if (CountryInfo.isSingapore()) {
            subPackages = sortByShelfNum(subPackages);
        }
        listAdapter.changeSubPackagesDate(subPackages);
    }

    final Set<String> set = new HashSet<>();
    boolean[] chooseTags;

    private void showNHStationTags() {
        if (set.size() == 0) {
            for (TSubPackage pkg : subPackages) {
                if (pkg.station != null) {
                    set.add(pkg.station.sortBy);
                }
            }
        }
        if (set.size() == 0) return;
        final String[] strings = set.toArray(new String[set.size()]);
        Arrays.sort(strings);
        chooseTags = new boolean[strings.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMultiChoiceItems(strings, chooseTags, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                chooseTags[which] = isChecked;
            }
        });
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ArrayList<String> tags = new ArrayList<>();
                for (int i = 0; i < chooseTags.length; i++) {
                    if (chooseTags[i]) {
                        tags.add(strings[i]);
                    }
                }
                ArrayList<TSubPackage> tSubPackages = filterByTag(tags);
                listAdapter.changeSubPackagesDate(tSubPackages);
            }
        });
        builder.show();
    }

    private ArrayList<TSubPackage> filterByTag(ArrayList<String> tags) {
        ArrayList<TSubPackage> tagsSubpackages = new ArrayList<>();
        if (subPackages == null || subPackages.size() <= 0) {
            return tagsSubpackages;
        }
        for (TSubPackage spkg : subPackages) {
            for (String tag : tags) {
                if (spkg.station != null && spkg.station.sortBy.equals(tag)) {
                    tagsSubpackages.add(spkg);
                }
            }
        }
        return tagsSubpackages;
    }

    private void sortByNhStationTag() {
        ArrayList<TSubPackage> subPackages = listAdapter.getListDate();
        if (subPackages == null || subPackages.size() <= 0) {
            return;
        }
        if (listAdapter == null) return;
        PickingActivity.NHStationComparator myComparator = new PickingActivity.NHStationComparator();
        Collections.sort(subPackages, myComparator);
        listAdapter.changeSubPackagesDate(subPackages);
    }

    /**
     * 点击用户名通过nickName排序
     */
    public static class NickNameComparator implements Comparator<TSubPackage> {

        @Override
        public int compare(TSubPackage parcel1, TSubPackage parcel2) {

            return parcel1.nickName.compareTo(parcel2.nickName);
        }

    }

    /**
     * 排序算法需求：
     * 1.总体排序按照shelfNum的前两位来增序；
     * 2.如果shelfNum的前2位是奇数：--> 则按照增序排列后面的字符；
     * 3.如果shelfNum的前2位是偶数：--> 3,4 位倒序递减，后面的正序递增；
     * <p/>
     * 排出的效果类似于：010A101B 010B101B 020B101P 020A102P 020A103P 030A101P 030B101P
     * <p/>
     * 解决思路：
     * 1.将packages 分为偶数List 奇数List 以及其他不匹配规则的otherList；
     * 2.将奇数增序排列；偶数降序排列
     * 3.将奇数和偶数组成一个新的数列
     * 4.按照shelfNum字符的前两个字符增序排序；
     *
     * @param packages
     * @return
     */
    public static ArrayList<TSubPackage> sortByShelfNum(ArrayList<TSubPackage> packages) {
        ArrayList<TSubPackage> jiShuList = new ArrayList<>();
        ArrayList<TSubPackage> ouShuList = new ArrayList<>();
        ArrayList<TSubPackage> otherList = new ArrayList<>();
        ArrayList<TSubPackage> allList = new ArrayList<>();

        for (int i = 0; i < packages.size(); i++) {
            if (!TextUtils.isEmpty(packages.get(i).shelfNum)) {
                String num = packages.get(i).shelfNum.substring(0, 2);
                try {
                    int number = Integer.parseInt(num);
                    if (number % 2 == 0) {
                        ouShuList.add(packages.get(i));
                    } else {
                        jiShuList.add(packages.get(i));
                    }
                } catch (NumberFormatException e) {
                    otherList.add(packages.get(i));
                }
            } else {
                otherList.add(packages.get(i));
            }
        }
        Collections.sort(jiShuList, new IncreaseComparator());
        Collections.sort(ouShuList, new DecreaseComparator());//排三四位
        Collections.sort(ouShuList, new IncreaseComparator_());//如果三四位相同正序排列
        allList.addAll(jiShuList);
        allList.addAll(ouShuList);
        Collections.sort(allList, new AllComparator());
        allList.addAll(otherList);
        return allList;
    }


    /**
     * 按照shelfNum 增序 排序
     */
    public static class IncreaseComparator implements Comparator<TSubPackage> {

        @Override
        public int compare(TSubPackage parcel1, TSubPackage parcel2) {

            return parcel1.shelfNum.substring(2, parcel1.shelfNum.length()).compareTo(parcel2.shelfNum.substring(2, parcel2.shelfNum.length()));
        }

    }

    /**
     * 偶数的数列按照shelfNumber 3 4 位数降序排序
     */
    public static class DecreaseComparator implements java.util.Comparator<TSubPackage> {

        @Override
        public int compare(TSubPackage parcel1, TSubPackage parcel2) {
            return parcel2.shelfNum.substring(2, 4).compareTo(parcel1.shelfNum.substring(2, 4));
        }

    }

    /**
     * 偶数的数列如果第三位 第四位一样 则按照shelfNumber 第四位数后面的按照增序排序
     */
    public static class IncreaseComparator_ implements Comparator<TSubPackage> {

        @Override
        public int compare(TSubPackage parcel1, TSubPackage parcel2) {
            if (parcel1.shelfNum.substring(2, 4).equals(parcel2.shelfNum.substring(2, 4))) {
                return parcel1.shelfNum.substring(4, parcel1.shelfNum.length()).compareTo(parcel2.shelfNum.substring(2, parcel2.shelfNum.length()));
            } else {
                return 0;
            }
        }

    }

    public static class AllComparator implements Comparator<TSubPackage> {

        @Override
        public int compare(TSubPackage parcel1, TSubPackage parcel2) {
            return parcel1.shelfNum.substring(0, 2).compareTo(parcel2.shelfNum.substring(0, 2));
        }

    }

    /**
     * 点击货架号通过shelfNum排序
     */
    public static class ShelfComparator implements Comparator<TSubPackage> {

        @Override
        public int compare(TSubPackage parcel1, TSubPackage parcel2) {

            return parcel1.shelfNum.compareTo(parcel2.shelfNum);
        }

    }

    public static class NHStationComparator implements Comparator<TSubPackage> {

        @Override
        public int compare(TSubPackage parcel1, TSubPackage parcel2) {
            if (parcel1.station == null || parcel2.station.sortBy == null) return 0;
            return parcel1.station.sortBy.compareTo(parcel2.station.sortBy);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (progressDialog.isShowing()) {
            progressDialog.hide();
        }
    }

    public ArrayList<TSubPackage> showBSubPackage() {
        ArrayList<TSubPackage> BList = new ArrayList<>();
        if (subPackages != null && subPackages.size() > 0) {
            for (TSubPackage subPackage : subPackages) {
                String BP = listAdapter.getBPMap().get(subPackage.parcelNum);
                if ("B".equals(BP)) {
                    BList.add(subPackage);
                }
            }
            listAdapter.changeSubPackagesDate(CountryInfo.isSingapore() ? sortByShelfNum(BList) : BList);
            return BList;
        }
        return null;
    }

    public ArrayList<TSubPackage> showPSubPackage() {
        ArrayList<TSubPackage> PList = new ArrayList<>();
        if (subPackages != null && subPackages.size() > 0) {
            for (TSubPackage subPackage : subPackages) {
                String BP = listAdapter.getBPMap().get(subPackage.parcelNum);
                if ("P".equals(BP)) {
                    PList.add(subPackage);
                }
            }
            listAdapter.changeSubPackagesDate(CountryInfo.isSingapore() ? sortByShelfNum(PList) : PList);
            return PList;
        }
        return null;
    }
}
