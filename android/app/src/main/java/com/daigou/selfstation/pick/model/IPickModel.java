package com.daigou.selfstation.pick.model;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.rpc.selfstation.TDeliveryMethod;
import com.daigou.selfstation.rpc.selfstation.TFindSubPackageResult;
import com.daigou.selfstation.rpc.selfstation.TSaveResult;
import com.daigou.selfstation.rpc.selfstation.TSaveSubPkgInfo;

import java.util.ArrayList;

/**
 * Created by 何祥源 on 16/4/27.
 * Desc:
 */
public interface IPickModel {
    ArrayList<RpcRequest> requests = new ArrayList<>();

    void loadDeliveryMethod(Response.Listener<ArrayList<TDeliveryMethod>> listener);

    void loadDriver(Response.Listener<ArrayList<String>> listener);

    void save(String subPackageNum, String BOrP, int packageId, int shipmentId, String boxNum, boolean isForceSave, Response.Listener<String> listener);

    void loadHouses(Response.Listener<ArrayList<String>> listResponse);

    void search(String deliveryCode, String startDate, String endDate, boolean isPicked, String driverOrStation, String periodOrNeighborhoodStation, int house, String AMPM,Response.Listener<TFindSubPackageResult> listener);

    void loadPeriod(String stationName, Response.Listener<ArrayList<String>> listener);

    void loadMRTStations(boolean addAM,Response.Listener<ArrayList<String>> listener);

    void loadNeighborhoodStations(String string , Response.Listener<ArrayList<String>> listener);

    void setParcelsIsLocked(ArrayList<Integer> shipmentIds, boolean isLocked, Response.Listener<String> listener);

    void saveSubPkgs(String boxNum,ArrayList<TSaveSubPkgInfo> pkgInfos,Response.Listener<TSaveResult> listener);

    void UserGetNeighborhoodStations(boolean addAM,Response.Listener<ArrayList<String>> listener);
}
