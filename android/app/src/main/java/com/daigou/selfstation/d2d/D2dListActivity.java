package com.daigou.selfstation.d2d;

import android.app.DatePickerDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.R;
import com.daigou.selfstation.activity.EzBaseActivity;
import com.daigou.selfstation.adapter.D2DAdapter;
import com.daigou.selfstation.rpc.selfstation.D2DService;
import com.daigou.selfstation.rpc.selfstation.TDeliveryJob;
import com.daigou.selfstation.rpc.selfstation.TFilter;
import com.daigou.selfstation.utils.CountryInfo;
import com.daigou.selfstation.utils.SharePreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by 何祥源 on 16/7/5.
 * Desc:
 */
public class D2dListActivity extends EzBaseActivity {
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recyclerView;
    private D2DAdapter adapter;
    public static final String[] items = {"day", "night", "big (10:00 AM – 7:00 PM)", "big (9:00 AM – 4:00 PM)", "big (3:00 PM – 10:00 PM)"};
    public static final String[] itemsShift = {"day", "night", "big", "big", "big"};
    public static final String[] timePeriod = {"", "", "10:00 AM – 7:00 PM", "9:00 AM – 4:00 PM", "3:00 PM – 10:00 PM"};
    private TFilter filter = new TFilter();
    private int filterShift = 0;

    private ArrayList<RpcRequest> requests = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d2d_list);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        filterShift = SharePreferenceUtils.getInt(getApplicationContext(), "filterShift", 0);
        adapter = new D2DAdapter();
        recyclerView.setAdapter(adapter);
        if (items.length > filterShift) {
            filter.shift = itemsShift[filterShift];
            filter.periodName = timePeriod[filterShift];
        } else {
            filter.shift = itemsShift[0];
            filter.periodName = timePeriod[0];
        }

        loadDelivery();

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDelivery();
            }
        });

        bindService(new Intent(this, LocationService.class), connection, Service.BIND_AUTO_CREATE);
    }

    private void loadDelivery() {
        if (!swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(true);
        }
        RpcRequest request = D2DService.UserFindDeliveryJobs(filter, new Response.Listener<ArrayList<TDeliveryJob>>() {
            @Override
            public void onResponse(ArrayList<TDeliveryJob> response) {
                if (swipeContainer.isRefreshing()) {
                    swipeContainer.setRefreshing(false);
                }
                if (response != null) {
                    adapter.setList(response);
                } else {
                    adapter.setList(null);
                }
            }
        });
        requests.add(request);
    }

    @Override
    protected void onDestroy() {
        for (int i = 0; i < requests.size(); i++) {
            requests.get(i).cancel();
        }
        unbindService(connection);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_d2d, menu);
        MenuItem item = menu.findItem(R.id.menu_switch);
        if (CountryInfo.isMalaysia()) {
            item.setVisible(false);
            filterShift = 0;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_switch:
                switchMenu();
                return true;
            case R.id.menu_date:
                chooseDate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void switchMenu() {
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(items, filterShift > items.length ? 0 : filterShift, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        filter.shift = itemsShift[selectedPosition];
                        filter.periodName = timePeriod[selectedPosition];
                        filterShift = selectedPosition;
                        SharePreferenceUtils.putInt(getApplicationContext(), "filterShift", selectedPosition);
                        loadDelivery();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void chooseDate() {
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.Dialog_DatePicker,
                // 绑定监听器
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        filter.dateInt = Integer.parseInt(simpleDateFormat.format(calendar.getTime()));
                        loadDelivery();
                    }
                }
                // 设置初始日期
                , calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar
                .get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private ServiceConnection connection = new ServiceConnection() {

        LocationService service;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            this.service = ((LocationService.ServiceBinder) service).service;
            this.service.startReport();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (service != null) {
                service.stopReport();
                service = null;
            }
        }
    };
}
