package com.daigou.selfstation.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daigou.selfstation.R;
import com.daigou.selfstation.d2d.JobDetailActivity;
import com.daigou.selfstation.rpc.selfstation.TDeliveryJob;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by undownding on 16-7-5.
 */
public class D2DAdapter extends RecyclerView.Adapter<D2DAdapter.ViewHolder> {

    private static final int ITEM_LAYOUT_ID = R.layout.item_list_d2d_activity;

    private ArrayList<TDeliveryJob> list = new ArrayList<>();

    public void setList(ArrayList<TDeliveryJob> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public D2DAdapter() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(ITEM_LAYOUT_ID, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TDeliveryJob item = list.get(position);
        bind(item, holder, position);
    }

    public void addData(List<TDeliveryJob> data) {
        list.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    private void bind(final TDeliveryJob item, ViewHolder viewHolder, int position) {
        viewHolder.address.setText("address:" + item.address);
        viewHolder.telephone.setText("tel:" + item.telephone);
        viewHolder.customerName.setText("customer name:" + item.customerName);
        viewHolder.nickName.setText("nick name:" + item.nickname);
        viewHolder.deliveryTime.setText("date:" + item.deliveryDate);
        viewHolder.postCode.setText("post:" + item.postCode);
        viewHolder.smsStatus.setText("sms:" + item.smsStatus);
        viewHolder.status.setText("status:" + item.status);
        viewHolder.eta.setText("eta:" + item.eta);
        viewHolder.workRemark.setText("work remark:" + item.remark);
        if (item.signTime == 0) {
            viewHolder.signTime.setVisibility(View.GONE);
        } else {
            CharSequence time = DateFormat.format("yyyy-MM-dd hh:mm:ss", item.signTime * 1000l);
            viewHolder.signTime.setText("sign time:" + time);
            viewHolder.signTime.setVisibility(View.VISIBLE);
        }
        viewHolder.num.setText(String.valueOf(position + 1));
        viewHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), JobDetailActivity.class);
                intent.putExtras(JobDetailActivity.setArguments(item));
                v.getContext().startActivity(intent);
            }
        });
        StringBuilder builder = new StringBuilder();
        if (item.shipments != null && item.shipments.size() > 0) {
            viewHolder.memo.setVisibility(View.VISIBLE);
            int size = item.shipments.size();
            for (int i = 0; i < size; i++) {
                builder.append("memo:" + item.shipments.get(i).memo + "\n");
                if (!TextUtils.isEmpty(item.shipments.get(i).remark)) {
                    builder.append("user remark:" + item.shipments.get(i).remark + "\n");
                }
            }
            viewHolder.memo.setText(builder.toString());
        } else {
            viewHolder.memo.setVisibility(View.GONE);
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private View root;
        private TextView address;
        private TextView telephone;
        private TextView customerName;
        private TextView nickName;
        private TextView deliveryTime;
        private TextView postCode;
        private TextView smsStatus;
        private TextView status;
        private TextView memo;
        private TextView workRemark;
        private TextView signTime;
        private TextView eta;
        private TextView num;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            address = (TextView) itemView.findViewById(R.id.tv_address);
            telephone = (TextView) itemView.findViewById(R.id.tv_telephone);
            customerName = (TextView) itemView.findViewById(R.id.tv_customer_name);
            nickName = (TextView) itemView.findViewById(R.id.tv_nick_name);
            deliveryTime = (TextView) itemView.findViewById(R.id.tv_delivery_time);
            postCode = (TextView) itemView.findViewById(R.id.tv_post_code);
            smsStatus = (TextView) itemView.findViewById(R.id.tv_sms_status);
            status = (TextView) itemView.findViewById(R.id.tv_status);
            memo = (TextView) itemView.findViewById(R.id.memo);
            eta = (TextView) itemView.findViewById(R.id.tv_eta);
            workRemark = (TextView) itemView.findViewById(R.id.tv_work_remark);
            num = (TextView) itemView.findViewById(R.id.position);
            signTime = (TextView) itemView.findViewById(R.id.tv_sign_time);
        }
    }
}
