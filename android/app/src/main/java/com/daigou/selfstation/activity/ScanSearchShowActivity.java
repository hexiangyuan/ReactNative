package com.daigou.selfstation.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.daigou.selfstation.R;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.rpc.selfstation.TSiblingSubPkgs;
import com.daigou.selfstation.utils.DensityUtil;
import com.daigou.selfstation.utils.ToastUtil;

import java.util.ArrayList;

/**
 * Creator:HeXiangYuan
 * Date  : 16-11-29
 */

public class ScanSearchShowActivity extends EzBaseActivity {
    private RecyclerView recyclerView;
    private TextView deliveryMethod;
    private TextView deliveryStation;
    private TextView deliveryTime;
    private TextView subPackageSize;
    private TextView searchFilter;
    private Adapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_search_detail);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        searchFilter = (TextView) findViewById(R.id.search_filter);
        deliveryMethod = (TextView) findViewById(R.id.delivery_method);
        deliveryStation = (TextView) findViewById(R.id.delivery_station);
        deliveryTime = (TextView) findViewById(R.id.delivery_time);
        subPackageSize = (TextView) findViewById(R.id.subPkgs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);
        String subPackage = getIntent().getStringExtra("subPackage");
        if (!TextUtils.isEmpty(subPackage)) {
            searchFilter.setText(String.format("Search Filter : %s", subPackage));
            load(subPackage);

        }
    }

    private void load(String subPackage) {
        DeliveryService.UserGetSiblingSubPkgs(subPackage, new Response.Listener<TSiblingSubPkgs>() {
            @Override
            public void onResponse(TSiblingSubPkgs response) {
                if (response != null) {
                    deliveryTime.setText(String.format("Delivery Date : %s",
                            response.deliveryDate));
                    deliveryMethod.setText(String.format("Delivery Method : %s",
                            response.deliveryMethod));
                    deliveryStation.setText(String.format("Delivery Station : %s",
                            response.stationName));
                    subPackageSize.setText(String.format("Total SubPackage : %s",
                            response.subPkgNumbers == null ? 0 : response.subPkgNumbers.size()));
                    adapter.setPkgses(response.subPkgNumbers);
                    adapter.notifyDataSetChanged();
                } else {
                    ToastUtil.showToast(R.string.Network_is_error);
                }
            }
        });
    }

    class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        ArrayList<String> pkgs;

        public void setPkgses(ArrayList<String> pkgs) {
            this.pkgs = pkgs;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setPadding(0, DensityUtil.dp2px(parent.getContext(), 8), 0, DensityUtil.dp2px(parent.getContext(), 8));
            return new ShowViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(pkgs.get(position));
        }

        @Override
        public int getItemCount() {
            return pkgs == null ? 0 : pkgs.size();
        }

        class ShowViewHolder extends RecyclerView.ViewHolder {

            public ShowViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
