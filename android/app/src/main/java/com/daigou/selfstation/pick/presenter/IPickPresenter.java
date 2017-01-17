package com.daigou.selfstation.pick.presenter;

import com.daigou.selfstation.rpc.selfstation.TSaveSubPkgInfo;

import java.util.ArrayList;

/**
 * Created by 何祥源 on 16/4/28.
 * Desc:
 */
public interface IPickPresenter {
    void showProgress();

    void hideProgress();

    void search(String deliveryCode, String startDate, String endDate, boolean isPicked, String driverOrStation, String periodOrNeighborhoodStation, int house,String AMPM);

    void pickedChecked();

    void deliveryMethodClicked();

    void startTimeClicked();

    void endTimeClicked();

    void isLock(ArrayList<Integer> list, boolean isLock);

    void showAllPeriod(String stationName);

    void showNeighborRegion();

    void showNeighborhoodStation(String stationName);

    void showAllDriver();

    void showHouses();

    void showMRTStations(boolean addAM);

    void showSelfCollectionStations();

    void checkAll();

    void clearCheck();

    void loadDeliveryMethod();

    void save(String subPkg, String BP, String boxNo, int pkgId, int shipmentId, boolean isForceSave);

    void saveSubPkgs(String boxNum, ArrayList<TSaveSubPkgInfo> pkgInfos);

    void userGetNeighborhoodRegions(boolean addAm);

    void cancelRpcRequests();
}
