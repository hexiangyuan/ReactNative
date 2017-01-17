package com.daigou.selfstation.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daigou.selfstation.R;
import com.daigou.selfstation.rpc.selfstation.TPickingJob;

import java.util.ArrayList;

/**
 * Created by wangxiang on 2016/11/14.
 */

public class MyJobAdapter extends RecyclerView.Adapter {
    private ArrayList<TPickingJob> data;
    private JobCallBack jobCallBack;

    public interface JobCallBack {
        void startPicking(String jobId);

        void selectTask(String jobId, int position);
    }

    public MyJobAdapter(ArrayList<TPickingJob> data, JobCallBack callBack) {
        this.data = data;
        this.jobCallBack = callBack;
    }

    public void setData(ArrayList<TPickingJob> data) {
        this.data = data;
    }

    public ArrayList<TPickingJob> getData() {
        return data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new JobViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_job, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        JobViewHolder jobViewHolder = (JobViewHolder) holder;
        jobViewHolder.bind(data.get(position), position);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    private class JobViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView delivery_method;
        TextView job_name;
        TextView bp;
        TextView status;
        TextView serial;
        TextView begin_picking;
        TextView task_transfer;
        TPickingJob job;
        int position;

        private JobViewHolder(View itemView) {
            super(itemView);
            delivery_method = (TextView) itemView.findViewById(R.id.delivery_method);
            job_name = (TextView) itemView.findViewById(R.id.job_name);
            bp = (TextView) itemView.findViewById(R.id.bp);
            status = (TextView) itemView.findViewById(R.id.status);
            serial = (TextView) itemView.findViewById(R.id.serial);
            begin_picking = (TextView) itemView.findViewById(R.id.begin_picking);
            begin_picking.setOnClickListener(this);
            task_transfer = (TextView) itemView.findViewById(R.id.task_transfer);
            task_transfer.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.begin_picking) {
                jobCallBack.startPicking(job.id);
            } else if (v.getId() == R.id.task_transfer) {
                jobCallBack.selectTask(job.id, position);
            }
        }

        void bind(TPickingJob job, int position) {
            this.position = position;
            this.job = job;
            delivery_method.setText(job.deliveryPeriod + "-" + job.deliveryMethod + "-" + job.stationOrDriver);
            job_name.setText(job.name);
            String bpValue = (job.b == 0 ? "" : job.b + "") + (job.p == 0 ? "" : job.p + "");
            bp.setText(bpValue);
            status.setText(job.status);
            serial.setText(String.valueOf(job.order));
        }
    }
}
