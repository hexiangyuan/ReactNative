package com.daigou.selfstation.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.daigou.selfstation.R;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.rpc.selfstation.TPrintLogInfo;
import com.daigou.selfstation.rpc.selfstation.TSubPackage;
import com.daigou.selfstation.view.RecycleViewDivider;

import java.util.ArrayList;

/**
 * Created by 何祥源 on 16/4/29.
 * Desc:
 */
public class RemarkInfoActivity extends AppCompatActivity {
    private ArrayList<TPrintLogInfo> list;
    public static final String REMARK_INFO = "RemarkInfo";
    public static final String PRINT_LOG = "PrintLog";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remark);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL));
        final Adapter adapter = new Adapter();
        recyclerView.setAdapter(adapter);
        TSubPackage subPackage = (TSubPackage) getIntent().getSerializableExtra("subPackage");
        String from = getIntent().getStringExtra("from");
        if (subPackage == null) return;
        if (from.equals(REMARK_INFO)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Remark Info");
            }
            DeliveryService.UserGetRemarkInfo(subPackage.customerId, subPackage.shipmentId, subPackage.packageId, new Response.Listener<ArrayList<TPrintLogInfo>>() {
                @Override
                public void onResponse(ArrayList<TPrintLogInfo> response) {
                    if (response != null) {
                        list = response;
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        } else if (PRINT_LOG.equals(from)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("PrintLog");
            }
            DeliveryService.UserGetPrintLogInfo(subPackage.packageId, subPackage.parcelNum, new Response.Listener<ArrayList<TPrintLogInfo>>() {
                @Override
                public void onResponse(ArrayList<TPrintLogInfo> response) {
                    if (response != null)
                        list = response;
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(RemarkInfoActivity.this).inflate(R.layout.item_recyclerview_remark, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ViewHolder) holder).bind();
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCreateBy, tvCreateDate, tvNotes;
            int position;

            public ViewHolder(View itemView) {
                super(itemView);
                tvCreateBy = (TextView) itemView.findViewById(R.id.create_by);
                tvCreateDate = (TextView) itemView.findViewById(R.id.create_date);
                tvNotes = (TextView) itemView.findViewById(R.id.notes);
            }

            public void bind() {
                position = getLayoutPosition();
                tvCreateDate.setText(list.get(position).createDate);
                tvCreateBy.setText(list.get(position).createBy);
                tvNotes.setText(list.get(position).note);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
