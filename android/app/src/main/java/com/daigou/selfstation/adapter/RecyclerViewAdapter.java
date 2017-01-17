package com.daigou.selfstation.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daigou.selfstation.R;
import com.daigou.selfstation.activity.ParcelDetailActivity;
import com.daigou.selfstation.rpc.selfstation.TParcel;
import com.daigou.selfstation.rpc.selfstation.TParcelSection;

import java.util.ArrayList;

/**
 * Created by 65grouper on 15/11/12.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ParcelListHolder>
        implements View.OnClickListener {
    private LayoutInflater layoutInflater;
    private Context context;
    private ArrayList<TParcel> parcels;
    private String status;

    public RecyclerViewAdapter(ArrayList<TParcel> parcels, Context context, String status) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.parcels = parcels;
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public ParcelListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ParcelListHolder(layoutInflater.inflate(R.layout.list_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ParcelListHolder holder, int position) {
        holder.tvUsername.setText(parcels.get(position).userName);
        holder.tvStatus.setText(parcels.get(position).status);
        holder.tvParcelNum.setText(parcels.get(position).parcelNumber);
        holder.tvPhone.setText(parcels.get(position).phone);
        if (parcels.get(position).sections.size() > 0) {
            holder.llValue.removeAllViews();
            for (TParcelSection tParcelSection : parcels.get(position).sections) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(14, 0, 0, 14);
                TextView textView = new TextView(context);
                textView.setLayoutParams(lp);
                textView.setText(tParcelSection.value);
                textView.setTextColor(Color.BLACK);
                holder.llValue.addView(textView);
            }
        }
        holder.setPosition(position);
    }

    @Override
    public int getItemCount() {
        return parcels == null ? 0 : parcels.size();
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();
        if (parcels != null && status.equals("arrived")) {
            Intent intent = new Intent(context, ParcelDetailActivity.class);
            intent.putExtra("ParcelNumber", parcels.get(position).parcelNumber);
            context.startActivity(intent);
        }
    }


    class ParcelListHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        TextView tvParcelNum;
        TextView tvPhone;
        TextView tvStatus;
        LinearLayout llParcel;
        LinearLayout llValue;

        public ParcelListHolder(View itemView) {
            super(itemView);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);
            tvParcelNum = (TextView) itemView.findViewById(R.id.tvParcelNum);
            tvPhone = (TextView) itemView.findViewById(R.id.tvPhone);
            tvStatus = (TextView) itemView.findViewById(R.id.tvStatus);
            llParcel = (LinearLayout) itemView.findViewById(R.id.ll_parcel);
            llValue = (LinearLayout) itemView.findViewById(R.id.ll_load_value);
            llParcel.setOnClickListener(RecyclerViewAdapter.this);
        }

        public void setPosition(int position) {
            llParcel.setTag(position);
        }
    }


}
