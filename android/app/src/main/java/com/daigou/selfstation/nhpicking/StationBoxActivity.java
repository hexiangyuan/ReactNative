package com.daigou.selfstation.nhpicking;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daigou.selfstation.R;
import com.daigou.selfstation.activity.EzBaseActivity;
import com.daigou.selfstation.rpc.selfstation.TParcelStationInfo;
import com.daigou.selfstation.rpc.selfstation.TSubPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by undownding on 2016/7/26.
 * <p/>
 * 这是一个把 地区 和 Box 绑定的情况展示出来；
 * 1.送搜索列表中获得parcel，并将parcel的Station去重 取出来;
 * 2.将 扫描绑定的Map中的数据取出来；
 * 3.组成一个新的Map k:String stationName; v : box；
 * 4.列表展示出来；
 */
public class StationBoxActivity extends EzBaseActivity {

    RecyclerView recyclerView;
    private ArrayList<TSubPackage> subPackages = new ArrayList<>();
    private HashMap<String, ArrayList<String>> date = new HashMap<>();
    private List<TParcelStationInfo> stations = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        setContentView(recyclerView);
        if (getIntent() != null) {
            subPackages = (ArrayList<TSubPackage>) getIntent().getSerializableExtra("list");
            stations = countStations(subPackages);//搜索列表中的所有stations,去重;
            HashMap<String, ArrayList<String>> scanStationNameBox = StationNameBoxManager.scanStationNameBoxNum;
            for (int i = 0; i < stations.size(); i++) {
                String stationSortBy = stations.get(i).stationName + ":" + stations.get(i).sortBy;
                date.put(stationSortBy, null);
            }
            for (String stationName : scanStationNameBox.keySet()) {
                date.put(stationName, scanStationNameBox.get(stationName));//如果有一样的stationName,就覆盖了stations的空值；
            }
            recyclerView.setAdapter(new MappingAdapter());
        }
    }


    private class MappingAdapter extends RecyclerView.Adapter<MappingAdapter.ViewHolder> {
        private ArrayList<Map.Entry<String, ArrayList<String>>> mDate = new ArrayList<>();

        public MappingAdapter() {
            mDate.addAll(date.entrySet());
        }

        @Override
        public MappingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_box_mapping, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final Map.Entry<String, ArrayList<String>> item = mDate.get(position);
            if (item.getValue() != null && item.getValue().size() > 0) {
                StringBuilder boxNums = new StringBuilder();
                for (int i =0;i<item.getValue().size();i++) {
                    if(i == 0){
                        boxNums.append("△"+item.getValue().get(i) + "\n");
                    }else{
                        boxNums.append(" "+item.getValue().get(i) + "\n");
                    }
                }
                holder.tvBoxCode.setText(boxNums.toString());
            } else {
                holder.tvBoxCode.setText("");
            }
            holder.tvStation.setText(item.getKey());
        }

        @Override
        public int getItemCount() {
            return mDate == null ? 0 : mDate.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            View root;
            TextView tvBoxCode;
            TextView tvStation;

            public ViewHolder(View itemView) {
                super(itemView);
                root = itemView;
                tvBoxCode = (TextView) itemView.findViewById(R.id.tv_box_code);
                tvStation = (TextView) itemView.findViewById(R.id.tv_station);
            }
        }
    }

    /**
     * 检查去重是否有相同的站点；
     *
     * @param packages
     * @return
     */
    public static List<TParcelStationInfo> countStations(ArrayList<TSubPackage> packages) {
        List<TParcelStationInfo> result = new ArrayList<>();
        for (TSubPackage subPackage : packages) {
            if (subPackage.station != null) {
                boolean flag = false;
                for (TParcelStationInfo station : result) {
                    if (station.stationName.equals(subPackage.station.stationName) && station.sortBy.equals(subPackage.station.sortBy)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    result.add(subPackage.station);
                }
            }
        }
        return result;
    }

}
