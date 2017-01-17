package com.daigou.selfstation.pick.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.daigou.selfstation.R;
import com.daigou.selfstation.activity.RemarkInfoActivity;
import com.daigou.selfstation.pick.presenter.IPickPresenter;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.rpc.selfstation.TSubPackage;
import com.daigou.selfstation.utils.CountryInfo;
import com.daigou.selfstation.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 何祥源 on 16/4/28.
 * Desc:
 */
public class PickAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<TSubPackage> list;
    private boolean isPicked;
    private Context mContext;
    private IPickPresenter presenter;
    private HashMap<String, Boolean> checkMap = new HashMap<>();//用来记录每一个Item的checked状态；key为ParcelNo；
    private HashMap<String, String> boxNoMap = new HashMap<>();//用来记录每一个Item的boxNo，以及color；key为ParcelNo；
    private HashMap<String, String> BPMap = new HashMap<>();//用来记录每一个Item的BP状态；key为ParcelNo；
    private HashMap<Integer, Boolean> lockMap = new HashMap<>();//用来记录每一个Item的BP状态；key为ShipmentId；

    public PickAdapter(Context mContext, IPickPresenter presenter) {
        this.mContext = mContext;
        this.presenter = presenter;
    }

    public void setListData(ArrayList<TSubPackage> list) {
        this.list = list;
        initMap();
        notifyDataSetChanged();
    }

    public ArrayList<TSubPackage> getListDate() {
        return list;
    }

    public void search(ArrayList<TSubPackage> list) {
        this.list = list;
        initMap();
        checkMap.clear();
        notifyDataSetChanged();
    }

    public void checkAll() {
        initCheckMap(true);
        notifyDataSetChanged();
    }

    public void clearCheck() {
        initCheckMap(false);
        notifyDataSetChanged();
    }

    public void scanResult(String boxNum, ArrayList<String> parcels) {
        if (parcels.size() >= 0 && !"".equals(boxNum)) {
            for (String parcel : parcels)
                boxNoMap.put(parcel, boxNum);
        }
        notifyDataSetChanged();
    }


    public void changeLocked(boolean isLocked, ArrayList<Integer> shipmentIds) {
        for (Integer shipmentId : shipmentIds) {
            lockMap.put(shipmentId, isLocked);
        }
        notifyDataSetChanged();
    }

    public ArrayList<TSubPackage> getCheckedParcels() {
        ArrayList<TSubPackage> tSubPackages = new ArrayList<>();
        for (String key : checkMap.keySet()) {
            if (checkMap.get(key)) {
                tSubPackages.add(getSubParcelBySubNum(key));
            }
        }
        return tSubPackages;
    }

    public void setPicked(boolean isPicked) {
        this.isPicked = isPicked;
    }

    public HashMap<String, String> getBPMap() {
        return BPMap;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_recycle_view_pick, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).bindList();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }


    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        int position;
        TextView tvUserId, tvParcelNo, tvBoxNo, tvBP, tvBPNo, tvWeight, tvSave, tvShelfNo, tvRemark,tvStation;
        CheckBox check;
        LinearLayout llItem;
        TSubPackage tSubPackage;


        ViewHolder(View itemView) {
            super(itemView);
            llItem = (LinearLayout) itemView.findViewById(R.id.ll_item);
            check = (CheckBox) itemView.findViewById(R.id.checkbox);
            tvStation = (TextView) itemView.findViewById(R.id.station);
            tvUserId = (TextView) itemView.findViewById(R.id.user_id);
            tvParcelNo = (TextView) itemView.findViewById(R.id.parcel_no);
            tvShelfNo = (TextView) itemView.findViewById(R.id.shelf_no);
            tvBoxNo = (TextView) itemView.findViewById(R.id.box_no);
            tvBP = (TextView) itemView.findViewById(R.id.BP);
            tvBPNo = (TextView) itemView.findViewById(R.id.parcel_bp);
            tvWeight = (TextView) itemView.findViewById(R.id.weight);
            tvSave = (TextView) itemView.findViewById(R.id.save);
            tvRemark = (TextView) itemView.findViewById(R.id.remark);
            tvRemark.setOnClickListener(this);
            tvShelfNo.setOnClickListener(this);
            tvSave.setOnClickListener(this);
            tvParcelNo.setOnClickListener(this);
            tvUserId.setOnClickListener(this);
            tvBPNo.setOnClickListener(this);
            check.setOnCheckedChangeListener(this);
        }

        void bindList() {
            position = getLayoutPosition();
            tSubPackage = list.get(position);
            boolean isChecked = checkMap.get(tSubPackage.parcelNum) == null ? false : checkMap.get(tSubPackage.parcelNum);
            check.setChecked(isChecked);
            if (tSubPackage.station != null) {
                tvStation.setText(TextUtils.isEmpty(tSubPackage.station.sortBy) ? "" : tSubPackage.station.sortBy);
            }
            tvUserId.setText(tSubPackage.nickName);
            tvParcelNo.setText(tSubPackage.parcelNum);
            tvShelfNo.setText(tSubPackage.shelfNum);
            tvWeight.setText(String.valueOf(tSubPackage.Weight) + "KG");
            tvBPNo.setText(tSubPackage.boxCount + "B" + tSubPackage.packageCount + "P");
            tvBoxNo.setText(boxNoMap.get(tSubPackage.parcelNum) == null ? "" : boxNoMap.get(tSubPackage.parcelNum));
            tvBP.setText(BPMap.get(tSubPackage.parcelNum) == null ? "" : BPMap.get(tSubPackage.parcelNum));
            if (isPicked) {
                check.setChecked(false);
                check.setVisibility(View.INVISIBLE);
                tvBP.setVisibility(View.INVISIBLE);
                tvSave.setVisibility(View.INVISIBLE);
                tvUserId.setClickable(false);
                tvParcelNo.setClickable(false);
                tvBPNo.setClickable(false);
                tvShelfNo.setClickable(false);
            } else {
                check.setVisibility(View.VISIBLE);
                tvBP.setVisibility(View.VISIBLE);
                tvSave.setVisibility(View.VISIBLE);
                tvUserId.setClickable(true);
                tvParcelNo.setClickable(true);
                tvBPNo.setClickable(true);
                tvShelfNo.setClickable(true);

                if (TextUtils.isEmpty(tSubPackage.remark)) {
                    tvRemark.setVisibility(View.GONE);
                } else {
                    tvRemark.setVisibility(View.VISIBLE);
                    tvRemark.setText(tSubPackage.remark);
                }

                if (tSubPackage.isHandCreated) {
                    llItem.setBackgroundResource(R.color.orange);
                } else if (tSubPackage.isModifyed) {
                    llItem.setBackgroundResource(R.color.light_blue);
                } else {
                    llItem.setBackgroundResource(android.R.color.white);
                }
                switch (tSubPackage.packageScanLabelColor) {
                    case "red":
                        tvUserId.setBackgroundResource(android.R.color.holo_red_light);
                        break;
                    case "green":
                        tvUserId.setBackgroundResource(android.R.color.holo_green_light);
                        break;
                    case "blue":
                        tvUserId.setBackgroundResource(android.R.color.holo_blue_light);
                        break;
                    case "yellow":
                        tvUserId.setBackgroundResource(R.color.yellow);
                        break;
                    case "orange":
                        tvUserId.setBackgroundResource(R.color.orange);
                        break;
                    default:
                        tvUserId.setBackgroundResource(android.R.color.transparent);
                        break;
                }
                if (lockMap.containsKey(tSubPackage.shipmentId) && lockMap.get(tSubPackage.shipmentId)) {
                    tvParcelNo.setTextColor(Color.RED);
                } else {
                    tvParcelNo.setTextColor(Color.BLACK);
                }
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.user_id:
                    userNameClicked();
                    break;
                case R.id.save:
                    saveClicked();
                    break;
                case R.id.parcel_no:
                    //Parcel 点击
                    if (tSubPackage.boxNums != null && tSubPackage.boxNums.size() > 0) {
                        showColorPick(tSubPackage.boxNums, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tvBoxNo.setText(tSubPackage.boxNums.get(which));
                                tSubPackage.boxNum = tSubPackage.boxNums.get(which);
                                boxNoMap.put(tSubPackage.parcelNum, tSubPackage.boxNums.get(which));
                            }
                        });
                    }
                    break;
                case R.id.parcel_bp:
                    showBPSelector(list.get(position));
                    break;
                case R.id.remark:
                    Intent i = new Intent(mContext, RemarkInfoActivity.class);
                    i.putExtra("subPackage", tSubPackage);
                    i.putExtra("from", RemarkInfoActivity.REMARK_INFO);
                    mContext.startActivity(i);
                    break;
                case R.id.shelf_no:
                    Intent intent = new Intent(mContext, RemarkInfoActivity.class);
                    intent.putExtra("subPackage", tSubPackage);
                    intent.putExtra("from", RemarkInfoActivity.PRINT_LOG);
                    mContext.startActivity(intent);
                default:
                    break;
            }
        }

        private void saveClicked() {
            if (TextUtils.isEmpty(tvBP.getText().toString())) {
                ToastUtil.showToast(R.string.You_must_choose_bp);
            }
            if (!boxNoMap.containsKey(tSubPackage.parcelNum)) {
                ToastUtil.showToast(R.string.You_must_choose_boxNum);
            } else {
                save(false);
            }
        }

        private void userNameClicked() {
            final ArrayList<String> colorList = new ArrayList<>();
            colorList.add("none");
            colorList.add("red");
            colorList.add("green");
            colorList.add("blue");
            colorList.add("yellow");
            colorList.add("orange");
            //更改颜色，
            showColorPick(colorList, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, final int which) {
                    colorList.get(which);
                    presenter.showProgress();
//                            由于pick逻辑异常复杂，不得不在Adapter中请求修改颜色的数据，不得以而为之；
                    DeliveryService.UserSavePackageScanLabelColor(tSubPackage.packageId, colorList.get(which), new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response != null) {
                                tSubPackage.packageScanLabelColor = colorList.get(which);
                                notifyDataSetChanged();
                            } else {
                                ToastUtil.showToast(R.string.Network_is_error);
                            }
                            presenter.hideProgress();
                        }
                    });
                }
            });
        }

        /**
         * 点击Save按钮保存数据，同样在adapter中请求了数据；555555555
         *
         * @param isForce
         */
        private void save(final boolean isForce) {
            presenter.showProgress();
            DeliveryService.UserSavePickSubPackage(tSubPackage.parcelNum, BPMap.get(tSubPackage.parcelNum), boxNoMap.get(tSubPackage.parcelNum), tSubPackage.packageId, tSubPackage.shipmentId, isForce, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    presenter.hideProgress();
                    if (response != null) {
                        if ("".equals(response)) {
                            Toast.makeText(mContext, R.string.save_succeeded, Toast.LENGTH_SHORT).show();
                            removeItem(tSubPackage);
                            notifyDataSetChanged();
                        } else {
                            showSaveFailMsg(response);
                        }
                    } else {
                        Toast.makeText(mContext, R.string.Network_is_error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == R.id.checkbox) {
                checkMap.put(list.get(position).parcelNum, isChecked);
            }
        }

        private void showSaveFailMsg(String response) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(response).setPositiveButton(R.string.force_save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    save(true);//强制保存
                }
            })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }
    }


    private void showColorPick(ArrayList<String> colorList, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, colorList);
        builder.setAdapter(adapter, listener);
        builder.show();
    }

    private void initCheckMap(boolean flag) {
        if (list == null || list.size() <= 0) return;
        checkMap.clear();
        int size = list.size();
        for (int i = 0; i < size; i++) {
//            二级包裹号
            TSubPackage subParcel = list.get(i);
            if (subParcel != null) {
                checkMap.put(subParcel.parcelNum, flag);
            }
        }
    }

    private void initMap() {
        if (list == null || list.size() <= 0) return;
        lockMap.clear();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            TSubPackage subParcel = list.get(i);
            if (subParcel != null) {
                //如果这个货架号的字符包含B，就判断这个二级包裹号是B，如果不是，就判断为P，在扫描装箱的提交的时候自动保存。
                //目前只适用于新加坡
                if (CountryInfo.isSingapore()) {
                    autoBP(subParcel);
                }
                boolean isChecked = checkMap.get(subParcel.parcelNum) == null ? false : checkMap.get(subParcel.parcelNum);
                checkMap.put(subParcel.parcelNum, isChecked);
                lockMap.put(subParcel.shipmentId, subParcel.isLocked);
                if (!TextUtils.isEmpty(subParcel.boxNum)) {
                    boxNoMap.put(subParcel.parcelNum, subParcel.boxNum);
                }
            }
        }
    }

    /**
     * 判断BP新规则：
     * 1.判断shelf num 最后一位数是B 或者P 如果是B 就是B 是P 就是P
     * 2.如果不是BP就判断是否包含B？ 包含B就是”B“ 否则就是P；
     *
     * @param subParcel
     */
    private void autoBP(TSubPackage subParcel) {
        if (!TextUtils.isEmpty(subParcel.shelfNum)) {
            String bp = "";
            if ("B".equals(subParcel.shelfNum.substring(subParcel.shelfNum.length() - 1, subParcel.shelfNum.length()))) {
                bp = "B";
            } else if ("P".equals(subParcel.shelfNum.substring(subParcel.shelfNum.length() - 1, subParcel.shelfNum.length()))) {
                bp = "P";
            } else {
                if (subParcel.shelfNum.contains("B")) {
                    bp = "B";
                } else {
                    bp = "P";
                }
            }
            if (!BPMap.containsKey(subParcel.parcelNum)) {
                BPMap.put(subParcel.parcelNum, bp);
            }
        }
    }

    private void showBPSelector(final TSubPackage subParcel) {
        if (subParcel == null) {
            return;
        }
        String[] items = {"B", "P"};
        new android.app.AlertDialog.Builder(mContext)
                .setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface d, int which) {
                        d.dismiss();
                        switch (which) {
                            case 0:
                                BPMap.put(subParcel.parcelNum, "B");
                                break;
                            case 1:
                                BPMap.put(subParcel.parcelNum, "P");
                                break;
                        }
                        changeOrder(subParcel);
                        notifyDataSetChanged();
                    }
                }).show();
    }

    private void changeOrder(TSubPackage subParcel) {
        if (list == null) return;
        if (subParcel != null) {
            list.remove(subParcel);
            list.add(subParcel);
        }
    }

    public TSubPackage getSubParcelBySubNum(String subNum) {
        if (subNum == null) return null;
        for (int i = 0; i < list.size(); i++) {
            TSubPackage subParcel = list.get(i);
            if (subParcel != null) {
                if (subNum.equals(subParcel.parcelNum)) {
                    return subParcel;
                }
            }
        }
        return null;
    }

    private void removeItem(TSubPackage tSubPackage) {
        list.remove(tSubPackage);
        boxNoMap.remove(tSubPackage.parcelNum);
        BPMap.remove(tSubPackage.parcelNum);
        checkMap.remove(tSubPackage.parcelNum);
        lockMap.remove(tSubPackage.shipmentId);
    }


    public void changeSubPackagesDate(ArrayList<TSubPackage> packages) {
        list = packages;
        notifyDataSetChanged();
    }
}
