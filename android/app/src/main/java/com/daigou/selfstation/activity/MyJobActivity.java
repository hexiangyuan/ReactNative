package com.daigou.selfstation.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.R;
import com.daigou.selfstation.adapter.MyJobAdapter;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.rpc.selfstation.TPicker;
import com.daigou.selfstation.rpc.selfstation.TPickingJob;
import com.daigou.selfstation.rpc.selfstation.TPickingJobFilter;
import com.daigou.selfstation.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class MyJobActivity extends AppCompatActivity implements MyJobAdapter.JobCallBack {
    ProgressDialog progressDialog;
    RecyclerView recyclerView;
    MyJobAdapter myJobAdapter = new MyJobAdapter(null, this);
    List<RpcRequest> requestList = new ArrayList<>();
    AlertDialog.Builder picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        picker = new AlertDialog.Builder(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        setContentView(R.layout.activity_my_job);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(myJobAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        TPickingJobFilter filter;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.pending:
                filter = new TPickingJobFilter();
                filter.status = "pending";
                getMyPickingJobs(filter);
                return true;
            case R.id.picking:
                filter = new TPickingJobFilter();
                filter.status = "picking";
                getMyPickingJobs(filter);
                return true;
            case R.id.done:
                filter = new TPickingJobFilter();
                filter.status = "done";
                getMyPickingJobs(filter);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_job, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < requestList.size(); i++) {
            RpcRequest request = requestList.get(i);
            if (request != null) {
                request.cancel();
            }
        }
        requestList = null;
    }

    private void getMyPickingJobs(TPickingJobFilter filter) {
        progressDialog.show();
        requestList.add(DeliveryService.UserGetMyPickingJobs(filter, new Response.Listener<ArrayList<TPickingJob>>() {
            public void onResponse(ArrayList<TPickingJob> response) {
                progressDialog.dismiss();
                if (response != null) {
                    myJobAdapter.setData(response);
                    myJobAdapter.notifyDataSetChanged();
                } else {
                    ToastUtil.showToast(R.string.Network_is_error);
                }
            }
        }));
    }

    @Override
    public void startPicking(final String jobId) {
        progressDialog.show();
        requestList.add(DeliveryService.UserStartPickingJob(jobId, new Response.Listener<String>() {
            public void onResponse(String response) {
                progressDialog.dismiss();
                if (response != null) {
                    if ("".equals(response)) {
                        Intent i = new Intent(MyJobActivity.this, StartPickingActivity.class);
                        i.putExtra("jobId", jobId);
                        startActivity(i);
                    } else {
                        ToastUtil.showToast(response);
                    }
                } else {
                    ToastUtil.showToast(R.string.Network_is_error);
                }
            }
        }));
    }

    int index;

    @Override
    public void selectTask(final String jobId, final int position) {
        progressDialog.show();
        requestList.add(DeliveryService.UserGetPickers(new Response.Listener<ArrayList<TPicker>>() {
            public void onResponse(final ArrayList<TPicker> response) {
                progressDialog.dismiss();
                if (response != null) {
                    final String[] ary = new String[response.size()];
                    for (int i = 0; i < response.size(); i++) {
                        ary[i] = response.get(i).pickerName;
                    }
                    picker.setSingleChoiceItems(ary, -1, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            index = which;
                        }
                    });
                    picker.setNegativeButton(android.R.string.cancel, null);
                    picker.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            taskTransfer(jobId, response.get(index).id, position);
                        }
                    }).show();
                } else {
                    ToastUtil.showToast(R.string.Network_is_error);
                }
            }
        }));
    }

    private void taskTransfer(String jobId, String pickerId, final int position) {
        progressDialog.show();
        requestList.add(DeliveryService.UserTransferPickingJob(jobId, pickerId, new Response.Listener<String>() {
            public void onResponse(String response) {
                progressDialog.dismiss();
                if (response != null) {
                    if ("".equals(response)) {
                        myJobAdapter.getData().remove(position);
                        myJobAdapter.notifyDataSetChanged();
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