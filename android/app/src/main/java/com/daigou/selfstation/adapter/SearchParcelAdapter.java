package com.daigou.selfstation.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daigou.selfstation.R;
import com.daigou.selfstation.rpc.selfstation.TParcel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 65grouper on 15/10/10.
 */
public class SearchParcelAdapter extends BaseAdapter {
    private ArrayList<TParcel> parcels;
    private Context mContext;
    private LayoutInflater layoutInflater;
    private HashMap<Integer, Boolean> isSelect;

    public SearchParcelAdapter(Context context) {
        mContext = context;
        layoutInflater = LayoutInflater.from(mContext);
        isSelect = new HashMap<>();
    }

    public void setParcels(ArrayList<TParcel> parcels) {
        this.parcels = parcels;
        if (parcels != null) {
            for (int i = 0; i < parcels.size(); i++) {
                isSelect.put(i, false);
            }
        }
    }

    public void addParcels(ArrayList<TParcel> parcels) {
        if (parcels != null) {
            int oldSize = this.parcels.size();
            this.parcels.addAll(parcels);
            for (int i = oldSize; i < this.parcels.size(); i++) {
                isSelect.put(i, false);
            }
        }
    }

    @Override
    public int getCount() {
        return parcels == null ? 0 : parcels.size();
    }

    @Override
    public Object getItem(int position) {
        return parcels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TParcel parcelItem = parcels.get(position);
        View view = convertView;
        final Holder holder;
        if (view == null) {
            holder = new Holder();
            view = layoutInflater.inflate(R.layout.parcel_list_items, parent, false);
            holder.tvUserID = (TextView) view.findViewById(R.id.tv_item_user_id);
            holder.tvPhone = (TextView) view.findViewById(R.id.tv_item_phone);
            holder.tvParcelNo = (TextView) view.findViewById(R.id.tv_item_parcel_no);
            holder.llSections = (LinearLayout) view.findViewById(R.id.ll_sections);
            holder.tvStatus = (TextView) view.findViewById(R.id.tv_item_status);
            holder.checkBox = (CheckBox) view.findViewById(R.id.cb_check);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }
        if (parcelItem != null) {
            holder.tvParcelNo.setText(parcelItem.parcelNumber);
            holder.tvUserID.setText(parcelItem.userName);
            holder.tvPhone.setText(parcelItem.phone);
            holder.tvStatus.setText(parcelItem.status);
            holder.checkBox.setOnClickListener(new CompoundButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isSelect.put(position, !isSelect.get(position));
                }
            });
            holder.checkBox.setChecked(isSelect.get(position));
            holder.llSections.removeAllViews();
            if (parcelItem.sections != null) {
                for (int i = 0; i < parcelItem.sections.size(); i++) {
                    LinearLayout linearLayout = new LinearLayout(mContext);
                    TextView tvName = new TextView(mContext);
                    tvName.setText(parcelItem.sections.get(i).name);
                    TextView tvValue = new TextView(mContext);
                    tvValue.setText(parcelItem.sections.get(i).value);
                    tvValue.setGravity(Gravity.RIGHT);
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    LinearLayout.LayoutParams valueLayoutParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    tvValue.setLayoutParams(valueLayoutParams);
                    linearLayout.setLayoutParams(lp);
                    linearLayout.addView(tvName);
                    linearLayout.addView(tvValue);
                    holder.llSections.addView(linearLayout);
                }
            }
        }
        return view;
    }


    private class Holder {
        private TextView tvUserID;
        private TextView tvPhone;
        private TextView tvParcelNo;
        private LinearLayout llSections;
        private TextView tvStatus;
        private CheckBox checkBox;
    }

    public ArrayList<TParcel> getCheckedParcel() {
        if (parcels == null || parcels.size() == 0) return null;
        ArrayList<TParcel> selectParcel = new ArrayList<>();
        for (int i = 0; i < parcels.size(); i++) {
            if (isSelect.get(i)) {
                selectParcel.add(parcels.get(i));
            }
        }
        return selectParcel;
    }
}
