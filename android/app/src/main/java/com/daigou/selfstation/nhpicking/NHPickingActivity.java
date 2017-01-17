package com.daigou.selfstation.nhpicking;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
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
import com.daigou.selfstation.pick.presenter.IPickPresenter;
import com.daigou.selfstation.pick.presenter.PickPresenterImpl;
import com.daigou.selfstation.pick.ui.PickAdapter;
import com.daigou.selfstation.pick.ui.PickingActivity;
import com.daigou.selfstation.pick.view.BasePickActivity;
import com.daigou.selfstation.pick.view.IPickView;
import com.daigou.selfstation.rpc.selfstation.TFindSubPackageResult;
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
import java.util.Set;

/**
 * Created by 何祥源 on 16/4/27.
 * Desc: pick Activity
 */
public class NHPickingActivity extends BasePickActivity implements IPickView, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private ArrayList<String> neighborhoodRegion = new ArrayList<>();
    private static ArrayList<TSubPackage> subPackages = new ArrayList<>();
    private int driverPosition = 0;
    private int housePosition = 0;
    private int currentBPPos = 0;
    private PickAdapter listAdapter;
    private IPickPresenter presenter;
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;
    private Button btnTimeStart, btnTimeEnd, btnDeliveryMen, btnWarehouses, btnLock, btnUnLock, btnSearch, btnBPNo, btnScan;
    private TextView tvTabNickName, tvTabShelfNum;
    private CheckBox picked, checkAll;
    private String str;
    private ProgressDialog progressDialog;
    private String currentDeliveryCode = "NeighbourhoodStation";
    private static final String[] BP = {"All", "B", "P"};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_picking_nh);
        presenter = new PickPresenterImpl(this);
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        initView();
        initToolbar();
        presenter.userGetNeighborhoodRegions(true);
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

        picked = (CheckBox) findViewById(R.id.cb_picked);
        checkAll = (CheckBox) findViewById(R.id.check_all);
        btnTimeStart = (Button) findViewById(R.id.time_start);
        btnTimeEnd = (Button) findViewById(R.id.time_end);
        btnDeliveryMen = (Button) findViewById(R.id.drivers);
        btnWarehouses = (Button) findViewById(R.id.warehouse);
        btnUnLock = (Button) findViewById(R.id.unlock);
        btnLock = (Button) findViewById(R.id.lock);
        btnSearch = (Button) findViewById(R.id.search);
        btnBPNo = (Button) findViewById(R.id.bp);
        btnScan = (Button) findViewById(R.id.scan);
        findViewById(R.id.station).setOnClickListener(this);
        btnTimeStart.setOnClickListener(this);
        btnTimeEnd.setOnClickListener(this);
        btnDeliveryMen.setOnClickListener(this);
        btnWarehouses.setOnClickListener(this);
        btnLock.setOnClickListener(this);
        btnUnLock.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnBPNo.setOnClickListener(this);
        btnScan.setOnClickListener(this);
        picked.setOnCheckedChangeListener(this);
        checkAll.setOnCheckedChangeListener(this);
        btnDeliveryMen.setHint(R.string.neighborhood);
        btnTimeStart.setText(simpleDateFormat.format(calendar.getTime()));
        btnTimeEnd.setText(simpleDateFormat.format(calendar.getTime()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                presenter.showHouses();
                break;
            case R.id.unlock:
                if (getCheckedShipmentIds() != null) {
                    presenter.isLock(getCheckedShipmentIds(), false);
                }
                break;
            case R.id.search:
                search();
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
                Intent i = new Intent(this, NHScanParcel2BoxActivity.class);
                NHScanParcel2BoxActivity.packages = subPackages;
                i.putExtra("BP", listAdapter.getBPMap());
                startActivityForResult(i, NHScanParcel2BoxActivity.REQUEST_CODE);
                break;
            case R.id.tab_nick_name://点击顶部的NickName 排序
                sortByNickName();
                break;
            case R.id.tab_shelf_num://点击顶部的shelfNum 排序
                sortByShelfNum();
                break;
            case R.id.station:
                showNHStationTags();
                sortByNhStationTag();
                break;
        }
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


    private void driversCase() {
        presenter.userGetNeighborhoodRegions(true);
    }

    private void search() {
        String startTime = btnTimeStart.getText().toString();
        String startEnd = btnTimeEnd.getText().toString();
        boolean isPicked = picked.isChecked();
        // 如果字段中包含All就传“”,索引所有的；
        String deliveryMen = btnDeliveryMen.getText().toString().contains("All") ? "" : btnDeliveryMen.getText().toString();
        int house = Integer.parseInt((btnWarehouses.getText().toString().equals("") || btnWarehouses.getText().toString().contains("All") ? "0" : btnWarehouses.getText().toString()));
        presenter.search(currentDeliveryCode, startTime, startEnd, isPicked, deliveryMen, "", house, "");
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 8888, 0, "Boxes").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case 8888:
                Intent i = new Intent(this, StationBoxActivity.class);
                i.putExtra("list", subPackages);
                startActivity(i);
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
                        break;
                    case R.id.warehouse:
                        housePosition = which;
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.show();
        return str;
    }

    /**
     * 搜索的结果回调方法；
     *
     * @param response
     */
    @Override
    public void searchClicked(TFindSubPackageResult response) {
        if (response != null) {
            btnBPNo.setText(response.totalBoxCount + "B" + response.totalPackageCount + "P");
            subPackages = response.subPackages;
            if (CountryInfo.isSingapore()) {//搜索后就排序！！！！！
                subPackages = PickingActivity.sortByShelfNum(subPackages);
            }
            listAdapter.search(subPackages);
            checkAll.setChecked(false);
        }

    }

    @Override
    public void showError() {
        Toast.makeText(NHPickingActivity.this, R.string.Network_is_error, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(NHPickingActivity.this, R.string.lock_succeeded, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(NHPickingActivity.this, R.string.unlock_succeeded, Toast.LENGTH_SHORT).show();
        }
        listAdapter.changeLocked(isLock, Shipments);
    }

    @Override
    public void showRegion() {
        showArrayListDialog(addAllInList(neighborhoodRegion, "All Region"), btnDeliveryMen, driverPosition);
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
    public void showStartTime() {
        showDateChooseDialog(btnTimeStart);
    }

    @Override
    public void showEndTime() {
        showDateChooseDialog(btnTimeEnd);
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
        StationNameBoxManager.scanStationNameBoxNum.clear();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        presenter.cancelRpcRequests();
        super.onDestroy();

    }

    protected ArrayList<String> addAllInList(ArrayList<String> list, String str) {
        list.add(0, str);
        return list;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == NHScanParcel2BoxActivity.REQUEST_CODE) {
            search();
        }
    }

    @Override
    public void neighborhoodRegionsLoaded(ArrayList<String> response) {
        neighborhoodRegion = response;
        presenter.showNeighborRegion();
    }

    private void sortByNickName() {
        ArrayList<TSubPackage> subPackages = listAdapter.getListDate();
        if (subPackages == null || subPackages.size() <= 0) {
            return;
        }
        if (listAdapter == null) return;
        NickNameComparator myComparator = new NickNameComparator();
        Collections.sort(subPackages, myComparator);
        listAdapter.setListData(subPackages);
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
            subPackages = PickingActivity.sortByShelfNum(subPackages);
        }
        listAdapter.setListData(subPackages);
    }

    /**
     * 点击用户名通过nickName排序
     */
    private class NickNameComparator implements Comparator<TSubPackage> {

        @Override
        public int compare(TSubPackage parcel1, TSubPackage parcel2) {
            return parcel1.nickName.compareTo(parcel2.nickName);
        }

    }

    /**
     * 点击货架号通过shelfNum排序
     */
    private class ShelfComparator implements Comparator<TSubPackage> {

        @Override
        public int compare(TSubPackage parcel1, TSubPackage parcel2) {
            return parcel1.shelfNum.compareTo(parcel2.shelfNum);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
        }
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

    public ArrayList<TSubPackage> showBSubPackage() {
        ArrayList<TSubPackage> BList = new ArrayList<>();
        if (subPackages != null && subPackages.size() > 0) {
            for (TSubPackage subPackage : subPackages) {
                String BP = listAdapter.getBPMap().get(subPackage.parcelNum);
                if ("B".equals(BP)) {
                    BList.add(subPackage);
                }
            }
            listAdapter.changeSubPackagesDate(CountryInfo.isSingapore() ? PickingActivity.sortByShelfNum(BList) : BList);
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
            listAdapter.changeSubPackagesDate(CountryInfo.isSingapore() ? PickingActivity.sortByShelfNum(PList) : PList);
            return PList;
        }
        return null;
    }
}
