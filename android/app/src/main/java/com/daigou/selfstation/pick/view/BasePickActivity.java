package com.daigou.selfstation.pick.view;

import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.daigou.selfstation.rpc.selfstation.TDeliveryMethod;
import com.daigou.selfstation.rpc.selfstation.TFindSubPackageResult;
import com.daigou.selfstation.rpc.selfstation.TSaveResult;

import java.util.ArrayList;

/**
 * Creator:HeXiangYuan
 * Date  : 16-11-28
 */

public abstract class BasePickActivity extends AppCompatActivity implements IPickView {
    public void showProgressBar() {
    }

    public void hideProgressBar() {
    }

    public String showDateChooseDialog(TextView textView) {
        return "";
    }

    public void searchClicked(TFindSubPackageResult response) {
    }

    public void showError() {
    }

    public void checkAll() {
    }

    public void clearCheck() {
    }

    public void lockSucceeded(boolean isLock, ArrayList<Integer> list) {
    }

    public void scan() {
    }

    public void showPeriod(ArrayList<String> response) {
    }

    public void showRegion() {
    }

    public void showNeighborhoodStations(ArrayList<String> response) {
    }

    public void showDrivers(ArrayList<String> response) {
    }

    public void showHouses(ArrayList<String> response) {
    }

    public void showMRTStation(ArrayList<String> response) {
    }

    public void showSelfCollections() {
    }

    public void showDeliveryMethod() {
    }

    public void showStartTime() {
    }

    public void showEndTime() {
    }

    public void isPickChecked() {
    }

    public void methodLoadFinished(ArrayList<TDeliveryMethod> response) {
    }

    public void saveClicked(String response) {
    }

    public void showSavedMsg(TSaveResult response) {
    }

    public void neighborhoodRegionsLoaded(ArrayList<String> response) {
    }
}
