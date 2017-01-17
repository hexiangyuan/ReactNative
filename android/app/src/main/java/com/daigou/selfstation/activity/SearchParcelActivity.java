package com.daigou.selfstation.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.R;
import com.daigou.selfstation.adapter.SearchParcelAdapter;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.rpc.selfstation.TCollectionStation;
import com.daigou.selfstation.rpc.selfstation.TDeliveryMethod;
import com.daigou.selfstation.rpc.selfstation.TPDTSmsMessage;
import com.daigou.selfstation.rpc.selfstation.TPDTSmsTemplate;
import com.daigou.selfstation.rpc.selfstation.TParcel;
import com.daigou.selfstation.utils.CountryInfo;
import com.daigou.selfstation.utils.ToastUtil;
import com.daigou.selfstation.view.LoadMoreListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by 65grouper on 15/10/9.
 */
public class SearchParcelActivity extends EzBaseActivity implements View.OnClickListener {
    private TextView tvLocation;
    private TextView tvMethod;
    private RelativeLayout rlDate;
    private TextView tvDate;
    private EditText etUserId;
    private EditText etPhone;
    private EditText etParcelNo;
    private EditText etShelNo;
    private Button btnSearch;
    private LoadMoreListView listView;
    private ArrayList<TParcel> parcels;
    private SearchParcelAdapter searchParcelAdapter;
    private View headerView;
    private int offset = 0;
    private int size = 10;
    private String methodStr, locationStr, dateStr, userIdStr, phoneStr, parcelNoStr, shelNoStr;
    private ArrayList<String> stationList;
    private Calendar calendar;
    private DatePickerDialog datePickerDialog;
    private ImageView imgClear;
    private SimpleDateFormat simpleDateFormat;
    private LinearLayout llProgressBar;
    private ArrayList<TDeliveryMethod> deliveryMethods = new ArrayList<>();
    private ArrayList<TCollectionStation> stations = new ArrayList<>();
    private int currentDeliveryMethodPosition, currentStationPosition;//用来记录SingleDialog选择的position
    private String currentMethodCode = "";
    private static final String CODE_HOME = "Home";
    private static final String CODE_SUBWAY = "Subway";
    private static final String CODE_NEIGHBOURHOOD_STATION = "NeighbourhoodStation";
    private ArrayList<RpcRequest> requests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_parcel);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        parcels = new ArrayList<>();
        initView();
        llProgressBar.setVisibility(View.VISIBLE);
        loadDeliverMethod();//加载deliveryMethod
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");


    }

    private void loadDeliverMethod() {
        DeliveryService.UserGetDeliveryMethod(new Response.Listener<ArrayList<TDeliveryMethod>>() {
            @Override
            public void onResponse(ArrayList<TDeliveryMethod> response) {
                if (response != null) {
                    llProgressBar.setVisibility(View.GONE);
                    deliveryMethods = response;
                    currentMethodCode = response.get(0).deliveryCode;
                    tvMethod.setText(deliveryMethods.get(0).deliveryName);
                }
            }
        });
    }


    private void initView() {
        listView = (LoadMoreListView) findViewById(R.id.list_view);
        headerView = LayoutInflater.from(this).inflate(R.layout.search_parcel_header, null, false);
        tvMethod = (TextView) headerView.findViewById(R.id.tv_method);
        tvLocation = (TextView) headerView.findViewById(R.id.tv_location);
        rlDate = (RelativeLayout) headerView.findViewById(R.id.rl_date);
        tvDate = (TextView) headerView.findViewById(R.id.tv_date);
        etPhone = (EditText) headerView.findViewById(R.id.et_phone);
        etUserId = (EditText) headerView.findViewById(R.id.et_user_id);
        etParcelNo = (EditText) headerView.findViewById(R.id.et_parcel_no);
        etShelNo = (EditText) headerView.findViewById(R.id.et_shel_no);
        btnSearch = (Button) headerView.findViewById(R.id.btn_search);
        imgClear = (ImageView) headerView.findViewById(R.id.clear);
        llProgressBar = (LinearLayout) findViewById(R.id.progress_bar);
        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDate.setText("");
            }
        });
        tvDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    imgClear.setVisibility(View.VISIBLE);
                } else {
                    imgClear.setVisibility(View.GONE);
                }
            }
        });
        listView.addHeaderView(headerView);
        listView.setHeaderDividersEnabled(false);
        tvMethod.setOnClickListener(this);
        tvLocation.setOnClickListener(this);
        rlDate.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        searchParcelAdapter = new SearchParcelAdapter(SearchParcelActivity.this);
        listView.setAdapter(searchParcelAdapter);
        listView.setOnLoadingListener(new LoadMoreListView.OnLoadingListener() {
            @Override
            public void onLoad() {
                loadData();
            }

        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SearchParcelActivity.this, ParcelDetailActivity.class);
                intent.putExtra("ParcelNumber", parcels.get(position - 1).parcelNumber);
                startActivity(intent);
            }
        });
    }

    /**
     * 判断所有的控件是否有选择或者输入
     *
     * @return true is not input anything
     */
    private boolean isAllEmpty() {
        if (tvMethod.getText().length() == 0 && tvLocation.getText().length() == 0 && etPhone.getText().length() == 0 &&
                etUserId.getText().length() == 0 && etParcelNo.getText().length() == 0
                && tvDate.getText().length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 加载数据
     */
    private void loadData() {
        llProgressBar.setVisibility(View.VISIBLE);
        RpcRequest request = DeliveryService.UserFindParcel(currentMethodCode, locationStr.equalsIgnoreCase("All") ? "" : locationStr, dateStr, userIdStr, phoneStr, parcelNoStr, shelNoStr, offset, size, new Response.Listener<ArrayList<TParcel>>() {
            @Override
            public void onResponse(ArrayList<TParcel> response) {
                if (response != null && response.size() > 0) {
                    if (offset == 0) {
                        searchParcelAdapter.setParcels(response);
                    } else {
                        searchParcelAdapter.addParcels(response);
                    }
                    searchParcelAdapter.notifyDataSetChanged();
                    listView.completeLoad();
                    offset += response.size();
                }
                llProgressBar.setVisibility(View.GONE);
            }
        });
        requests.add(request);
    }

    /**
     * 获得控件上的字符串
     */
    private void setData() {
        methodStr = tvMethod.getText().toString();
        locationStr = tvLocation.getText().toString();
        dateStr = tvDate.getText().toString();
        userIdStr = etUserId.getText().toString().trim();
        phoneStr = etPhone.getText().toString().trim();
        parcelNoStr = etParcelNo.getText().toString().trim();
        methodStr = tvMethod.getText().toString();
        shelNoStr = etShelNo.getText().toString();
        offset = 0;
    }

    /**
     * 弹出对话框选择station
     */
    private void loadStationByDeliveryCode() {
        switch (currentMethodCode) {
            case CODE_HOME: //不加载任何信息

                break;
            case CODE_SUBWAY://加载mrtStation
                showMRTStation();
                break;
            case CODE_NEIGHBOURHOOD_STATION:
                showNeighborhoodRegions();
                break;
            default:
                stations = deliveryMethods.get(currentDeliveryMethodPosition).collectionStations;
                if (stations != null && stations.size() > 0) {
                    stationList = stationsToList(stations);
                    stationList = addAllInList(stationList, "All");
                    showLocationDialog();
                }
                break;
        }

    }

    private void showNeighborhoodRegions() {
        llProgressBar.setVisibility(View.VISIBLE);
        RpcRequest request = DeliveryService.UserGetNeighborhoodRegions(false, new Response.Listener<ArrayList<String>>() {
            @Override
            public void onResponse(ArrayList<String> response) {
                llProgressBar.setVisibility(View.GONE);
                if (response != null) {
                    stationList = addAllInList(response, "All region");
                    showLocationDialog();
                } else {
                    Toast.makeText(SearchParcelActivity.this, R.string.Network_is_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        requests.add(request);
    }

    private void showMRTStation() {
        llProgressBar.setVisibility(View.VISIBLE);
        RpcRequest request = DeliveryService.UserGetMRTStations(false, new Response.Listener<ArrayList<String>>() {
            @Override
            public void onResponse(ArrayList<String> response) {
                llProgressBar.setVisibility(View.GONE);
                if (response != null) {
                    stationList = response;
                    stationList = addAllInList(stationList, "All");
                    showLocationDialog();
                } else {
                    Toast.makeText(SearchParcelActivity.this, R.string.Network_is_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        requests.add(request);
    }

    private ArrayList<String> stationsToList(ArrayList<TCollectionStation> collectionStations) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < collectionStations.size(); i++) {
            list.add(collectionStations.get(i).stationName);
        }
        return list;
    }

    /**
     * station singleChoiceDialog
     */
    private void showLocationDialog() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, stationList);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.Choice_station))
                .setSingleChoiceItems(adapter, currentStationPosition, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentStationPosition = which;
                        locationStr = stationList.get(which);
                        tvLocation.setText(locationStr);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * 选择日期
     */
    private void showDatePicker() {
        if (datePickerDialog == null) {
            datePickerDialog = new DatePickerDialog(SearchParcelActivity.this,
                    // 绑定监听器
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, monthOfYear);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            tvDate.setText(simpleDateFormat.format(calendar.getTime()));
                        }
                    }
                    // 设置初始日期
                    , calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar
                    .get(Calendar.DAY_OF_MONTH));

        }
        datePickerDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_method:
                choseMethod();
                break;
            case R.id.tv_location:
                loadStationByDeliveryCode();
                break;
            case R.id.rl_date:
                showDatePicker();
                break;
            case R.id.btn_search:
                if (isAllEmpty()) {
                    return;
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etParcelNo.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(etPhone.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(etUserId.getWindowToken(), 0);
                imm.hideSoftInputFromInputMethod(etShelNo.getWindowToken(), 0);
                setData();
                if (parcels != null) {
                    parcels.clear();
                }
                loadData();
                break;
        }
    }


    /**
     * 选择DeliveryMethod
     */
    private void choseMethod() {
        if (deliveryMethods == null || deliveryMethods.size() <= 0) return;
        final ArrayList<String> list = new ArrayList<>();
        for (TDeliveryMethod deliveryMethod : deliveryMethods) {
            list.add(deliveryMethod.deliveryName);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, list);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.Choice_delivery_method))
                .setSingleChoiceItems(adapter, currentDeliveryMethodPosition, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentDeliveryMethodPosition = which;
                        methodStr = deliveryMethods.get(which).deliveryName;
                        currentStationPosition = 0;//清楚station的选项；
                        currentMethodCode = deliveryMethods.get(which).deliveryCode;
                        locationStr = "";
                        tvLocation.setText(locationStr);
                        tvMethod.setText(list.get(which));
                        dialog.dismiss();
                    }
                })
                .show();
    }

    protected ArrayList<String> addAllInList(ArrayList<String> list, String str) {
        list.add(0, str);
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (CountryInfo.isMalaysia())
            getMenuInflater().inflate(R.menu.menu_search_parcel, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_sms:
                showSms();
                return true;
            default:
                return false;
        }
    }

    private void showSms() {
        ArrayList<TParcel> checkedParcel = searchParcelAdapter.getCheckedParcel();
        if (checkedParcel != null && checkedParcel.size() > 0) {
            //去除重复的联系人
            ArrayList<TParcel> tParcels = removeSameUser(checkedParcel);
            loadSmsTemplates(tParcels);
        } else {
            ToastUtil.showToast("Please select parcels");
        }
    }

    private void loadSmsTemplates(final ArrayList<TParcel> tParcels) {
        RpcRequest request = DeliveryService.UserPDTGetSMSTemplates(currentMethodCode, locationStr.contains("All") ? "" : locationStr, "", new Response.Listener<ArrayList<TPDTSmsTemplate>>() {
            @Override
            public void onResponse(ArrayList<TPDTSmsTemplate> response) {
                if (response != null && response.size() > 0) {
                    showSendSmsDialog(response, tParcels);
                } else {
                    ToastUtil.showToast("Failed to get sms templates,please try again.");
                }
            }
        });
        requests.add(request);
    }

    private void showSendSmsDialog(final ArrayList<TPDTSmsTemplate> response, final ArrayList<TParcel> tParcels) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = View.inflate(this, R.layout.dialog_layout_msg, null);
        RadioGroup rgTitle = (RadioGroup) v.findViewById(R.id.rg_title);
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
                etContent.setText(response.get(checkedId).content);
            }
        });
        builder.setView(v);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendSms(etContent.getText().toString(), tParcels);
            }
        }).setNegativeButton(android.R.string.cancel, null).show();
    }

    private void sendSms(String content, ArrayList<TParcel> tParcels) {
        if (TextUtils.isEmpty(content)) {
            ToastUtil.showToast("Please input Sms content.");
            return;
        }
        ArrayList<TPDTSmsMessage> msgs = new ArrayList<>();
        for (int i = 0; i < tParcels.size(); i++) {
            TPDTSmsMessage msg = new TPDTSmsMessage();
            msg.customerId = tParcels.get(i).customerId;
            msg.phoneNumber = tParcels.get(i).phone;
            msg.deliveryMethod = currentMethodCode;
            msg.stationName = locationStr.contains("All") ? "" : locationStr;
            msgs.add(msg);
        }
        DeliveryService.UserPDTSendCustomerSMS(msgs, content, new Response.Listener<String>() {
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

    private ArrayList<TParcel> removeSameUser(ArrayList<TParcel> parcels) {
        ArrayList<TParcel> newList = new ArrayList<>();
        for (TParcel parcel : parcels) {
            boolean b = false;
            for (int i = 0; i < newList.size(); i++) {
                if (newList.get(i).phone.equals(parcel.phone)) {
                    b = true;
                    break;
                }
            }
            if (!b) newList.add(parcel);
        }
        return newList;
    }
}
