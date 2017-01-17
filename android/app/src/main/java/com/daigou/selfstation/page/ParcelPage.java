package com.daigou.selfstation.page;


import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.android.volley.Response;
import com.daigou.selfstation.R;
import com.daigou.selfstation.adapter.RecyclerViewAdapter;
import com.daigou.selfstation.listener.OnRcvScrollListener;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.rpc.selfstation.TParcel;

import java.util.ArrayList;

/**
 * Created by DaZhuang on 15/10/14.
 *
 */

public class ParcelPage implements OnRcvScrollListener.OnLoadListener {
    private Context context;
    private View parcelListView;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private ArrayList<TParcel> parcelList;
    private RecyclerViewAdapter recyclerViewAdapter;
    private OnRcvScrollListener onRcvScrollListener;
    private LinearLayout llProgressBar;
    private int offset = 0;
    private int limit = 10;

    private String stationName;
    private String status;
    private ProgressDialog pd;

    public ParcelPage(Context context, String status, String stationName) {
        this.context = context;
        this.status = status;
        this.stationName = stationName;
        parcelList = new ArrayList<>();
        onRcvScrollListener = new OnRcvScrollListener();
        recyclerViewAdapter = new RecyclerViewAdapter(parcelList, context, status);
        getView();
    }

    public void updateData(String stationName, String status) {
        this.stationName = stationName;
        this.status = status;
        offset = 0;
        if (parcelList != null) {
            parcelList.clear();
            recyclerViewAdapter.setStatus(status);
            recyclerViewAdapter.notifyDataSetChanged();
            loadMoreData();
        }
    }

    public View getView() {
        parcelListView = LayoutInflater.from(context).inflate(R.layout.list_parcel, null);
        swipeRefresh = (SwipeRefreshLayout) parcelListView.findViewById(R.id.swipe_refresh);
        recyclerView = (RecyclerView) parcelListView.findViewById(R.id.list_view);
        llProgressBar = (LinearLayout) parcelListView.findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(recyclerViewAdapter);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData(stationName, status);
                swipeRefresh.setRefreshing(false);
            }
        });
        onRcvScrollListener.setOnLoadListener(this);
        recyclerView.setOnScrollListener(onRcvScrollListener);

        return parcelListView;
    }

    /**
     * 上拉加载更多数据
     */
    public void loadMoreData() {
        llProgressBar.setVisibility(View.VISIBLE);
        DeliveryService.UserListParcel(stationName, this.status, offset, limit,
                new Response.Listener<ArrayList<TParcel>>() {
            @Override
            public void onResponse(final ArrayList<TParcel> response) {
                if (response != null) {
                    if (response.size() > 0) {
                        onRcvScrollListener.setCompleted(false);
                        parcelList.addAll(response);
                        recyclerViewAdapter.notifyDataSetChanged();
                        offset += response.size();
                    } else {
                        onRcvScrollListener.setCompleted(true);
                    }
                }
                llProgressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onLoad() {
        loadMoreData();
    }
}

