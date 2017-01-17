package com.daigou.selfstation.pick.view;

import android.widget.TextView;

import com.daigou.selfstation.rpc.selfstation.TDeliveryMethod;
import com.daigou.selfstation.rpc.selfstation.TFindSubPackageResult;
import com.daigou.selfstation.rpc.selfstation.TSaveResult;

import java.util.ArrayList;

/**
 * Created by 何祥源 on 16/4/27.
 * Desc:pick activity model
 */
public interface IPickView {
    void showProgressBar();

    void hideProgressBar();

    String showDateChooseDialog(TextView textView);

    void searchClicked(TFindSubPackageResult response);

    void showError();

    void checkAll();

    void clearCheck();

    void lockSucceeded(boolean isLock, ArrayList<Integer> list);

    void scan();

    void showPeriod(ArrayList<String> response);

    void showRegion();

    void showNeighborhoodStations(ArrayList<String> response);

    void showDrivers(ArrayList<String> response);

    void showHouses(ArrayList<String> response);

    void showMRTStation(ArrayList<String> response);

    void showSelfCollections();

    void showDeliveryMethod();

    void showStartTime();

    void showEndTime();

    void isPickChecked();

    void methodLoadFinished(ArrayList<TDeliveryMethod> response);

    void saveClicked(String response);

    void showSavedMsg(TSaveResult response);

    void neighborhoodRegionsLoaded(ArrayList<String> response);
}
