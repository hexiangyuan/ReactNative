package com.daigou.selfstation.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.daigou.selfstation.R;
import com.daigou.selfstation.adapter.ViewPagerAdapter;
import com.daigou.selfstation.page.ParcelPage;
import com.daigou.selfstation.system.EzDeliveryApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DaZhuang on 15/10/9.
 */
public class ParcelListActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private List<View> viewContainer;
    private ParcelPage incomingPage;
    private ParcelPage arrivedPage;
    private ParcelPage completedPage;
    private List<String> stations;
    private String stationName;
    private TabLayout tabLayout;
    private SubMenu station;
    public static final int STATION_ID = 123;
    public static final int STATION_GROUP_ID = 321;
    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver;
    private MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcel_list);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        viewContainer = new ArrayList<>();
        stations = EzDeliveryApplication.getInstance().getLoginResult().StationNames;

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        if (stations.size() > 0) {
            // 首次加载第一个站点
            stationName = stations.get(0);
        } else {
            return;
        }
        initViewPage(stationName);
        tabLayout.setupWithViewPager(viewPager);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.daigou.selfstation.activity.ParcelDetailActivity.acknowledge");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().
                        equals("com.daigou.selfstation.activity.ParcelDetailActivity.acknowledge")) {
                    if (stationName != null && !stationName.equals("")) {
                        arrivedPage.updateData(stationName, "arrived");
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initViewPage(final String stationName) {
        if (viewContainer.size() > 0) {
            viewContainer.clear();
        }
        incomingPage = new ParcelPage(this, "incoming", stationName);
        arrivedPage = new ParcelPage(this, "arrived", stationName);
        completedPage = new ParcelPage(this, "completed", stationName);
        viewContainer.add(incomingPage.getView());
        viewContainer.add(arrivedPage.getView());
        viewContainer.add(completedPage.getView());
        incomingPage.loadMoreData();
        viewPager.setAdapter(new ViewPagerAdapter(viewContainer));
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    incomingPage.updateData(ParcelListActivity.this.stationName, "incoming");
                } else if (position == 1) {
                    arrivedPage.updateData(ParcelListActivity.this.stationName, "arrived");
                } else if (position == 2) {
                    completedPage.updateData(ParcelListActivity.this.stationName, "completed");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (stations != null && stations.size() != 0) {
            station = menu.addSubMenu(0, STATION_ID, 0, stationName);
            station.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            for (int i = 0; i < stations.size(); i++) {
                station.add(STATION_GROUP_ID, i, i, stations.get(i));
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.item = item;
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getGroupId() == STATION_GROUP_ID) {
            station.getItem().setTitle(stations.get(item.getItemId()));
            stationName = stations.get(item.getItemId());
            switch (viewPager.getCurrentItem()) {
                case 0:
                    incomingPage.updateData(stations.get(item.getItemId()), "incoming");
                    break;
                case 1:
                    arrivedPage.updateData(stations.get(item.getItemId()), "arrived");
                    break;
                case 2:
                    completedPage.updateData(stations.get(item.getItemId()), "completed");
                    break;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
        super.onDestroy();
    }
}