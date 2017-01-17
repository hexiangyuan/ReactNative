package com.daigou.selfstation.pick.model;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.rpc.selfstation.TDeliveryMethod;
import com.daigou.selfstation.rpc.selfstation.TFindSubPackageResult;
import com.daigou.selfstation.rpc.selfstation.TSaveResult;
import com.daigou.selfstation.rpc.selfstation.TSaveSubPkgInfo;

import java.util.ArrayList;

/**
 * Created by 何祥源 on 16/4/28.
 * Desc:
 */
public class PickModelImpl implements IPickModel {

    @Override
    public void loadDeliveryMethod(Response.Listener<ArrayList<TDeliveryMethod>> listener) {
        RpcRequest request = DeliveryService.UserGetDeliveryMethod(listener);
        requests.add(request);
    }

    @Override
    public void loadDriver(Response.Listener<ArrayList<String>> listener) {
        RpcRequest request = DeliveryService.UserGetHomeDeliveryDrivers(listener);
        requests.add(request);
    }

    @Override
    public void save(String subPackageNum, String BOrP, int packageId, int shipmentId, String boxNum, boolean isForceSave, Response.Listener<String> listener) {

    }

    @Override
    public void loadHouses(Response.Listener<ArrayList<String>> listResponse) {
        RpcRequest request = DeliveryService.UserGetDeliveryWarehouse(listResponse);
        requests.add(request);
    }

    @Override
    public void search(String deliveryCode, String startDate, String endDate, boolean isPicked, String driverOrStation, String periodOrNeighborhoodStation, int house, String AMPM,Response.Listener<TFindSubPackageResult> listener) {
        RpcRequest request = DeliveryService.UserFindPackageNumbers(deliveryCode, startDate, endDate, driverOrStation, periodOrNeighborhoodStation, false, isPicked, house, false, false,AMPM,false,listener);
        requests.add(request);
    }

    @Override
    public void loadPeriod(String stationName, Response.Listener<ArrayList<String>> listener) {
        RpcRequest request = DeliveryService.UserGetCollectionPeriod(stationName, listener);
        requests.add(request);
    }

    @Override
    public void loadMRTStations(boolean addAM, Response.Listener<ArrayList<String>> listener) {
        RpcRequest request = DeliveryService.UserGetMRTStations(addAM, listener);
        requests.add(request);
    }

    @Override
    public void loadNeighborhoodStations(String string, Response.Listener<ArrayList<String>> listener) {
        RpcRequest request = DeliveryService.UserGetNeighborhoodStations(string, listener);
        requests.add(request);
    }

    @Override
    public void setParcelsIsLocked(ArrayList<Integer> shipmentIds, boolean isLocked, Response.Listener<String> listener) {
        RpcRequest request = DeliveryService.UserSetShipmentStatus(shipmentIds, isLocked, listener);
        requests.add(request);
    }

    @Override
    public void saveSubPkgs(String boxNum,ArrayList<TSaveSubPkgInfo> pkgInfo, Response.Listener<TSaveResult> listener) {
        RpcRequest request = DeliveryService.UserSavePickSubPackages(pkgInfo,boxNum, listener);
        requests.add(request);
    }

    @Override
    public void UserGetNeighborhoodStations(boolean addAM, Response.Listener<ArrayList<String>> listener) {
        RpcRequest request = DeliveryService.UserGetNeighborhoodRegions(addAM,listener);
        requests.add(request);
    }
}
