package com.daigou.selfstation.nhpicking;

import android.content.DialogInterface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.R;
import com.daigou.selfstation.activity.ScanParcel2BoxActivity;
import com.daigou.selfstation.pick.ui.PickingActivity;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.rpc.selfstation.TParcelStationInfo;
import com.daigou.selfstation.rpc.selfstation.TSubPackage;
import com.daigou.selfstation.utils.CountryInfo;
import com.daigou.selfstation.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by undownding on 16-7-22.
 */
public class NHScanParcel2BoxActivity extends ScanParcel2BoxActivity {

    public static ArrayList<TSubPackage> packages = new ArrayList<>();

    private ArrayList<String> scanBoxes = new ArrayList<>();

    private ArrayList<String> scanParcels = new ArrayList<>();

    private HashMap<String, String> BPMap = new HashMap<>();//用来记录每一个Item的BP状态；key为ParcelNo；

    private RpcRequest saveRequest;
    private TextToSpeech tts;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getArgs();
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        ToastUtil.showToast("Language is not available.");
                    } else {
                        tts.setSpeechRate(0.8f);
                        tts.setPitch(0.5f);
                    }
                } else {
                    ToastUtil.showToast("Could not initialize TextToSpeech.");
                }
            }
        });
    }

    @Override
    protected void ScanBox(String code) {
        tts.stop();
        if (code != null && code.length() > 0 && CountryInfo.isSingapore()) {
            String speak = code.substring(code.length() - 1, code.length());
            tts.speak(speak, TextToSpeech.QUEUE_FLUSH, null);
        }
        String stationNameSortBy = getStationNameByBox(code);
        if (stationNameSortBy == null) {
            mpFailed.start();
            ToastUtil.showToast(code + getString(R.string.isnot_correct_format));
            restartPreviewAfterDelay(2000);
            return;
        }
        //判断box是否已经扫描过了！
        for (int i = 0; i < scanBoxes.size(); i++) {
            if (scanBoxes.get(i).equals(code)) {
                ToastUtil.showToast(code + getString(R.string.has_scanned));
                mpFailed.start();
                restartPreviewAfterDelay(2000);
                return;
            }
        }
        //判断box是否已经和station 绑定了
        String boxCode = splitBoxNo(code);
        if (StationNameBoxManager.scanStationNameBoxNum.containsKey(stationNameSortBy)) {
            ArrayList<String> boxes = StationNameBoxManager.scanStationNameBoxNum.get(stationNameSortBy);
            boxes = boxes == null ? new ArrayList<String>() : boxes;
            boxes.add(0, boxCode);
            StationNameBoxManager.scanStationNameBoxNum.put(stationNameSortBy, boxes);
        } else {
            ArrayList<String> boxes = new ArrayList<>();
            boxes.add(boxCode);
            StationNameBoxManager.scanStationNameBoxNum.put(stationNameSortBy, boxes);
        }
        scanBoxes.add(boxCode);
        mpSucceeded.start();
        tvBoxNum.setText(scanBoxes.toString());
        restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
    }

    /**
     * 获取StationName 通过拼接规则
     *
     * @param code boxNum
     * @return stationName + ":" +sortBy；
     */
    private String getStationNameByBox(String code) {
        if (!code.contains(";")) {
            return null;
        }
        try {
            String[] s = code.split(";");
            String stationName = s[s.length - 3];
            String sortBy = s[s.length - 1];
            return stationName + ":" + sortBy;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * 如果有分号的话截取分号的第一段；
     * 没有分号传全部
     *
     * @param code
     */
    public String splitBoxNo(String code) {
        if (TextUtils.isEmpty(code)) return "";
        if (!code.contains(";")) {
            return code;
        }
        String[] result = code.split(";");
        if (result.length >= 1) {
            code = result[0];
        }
        return code;

    }

    /**
     * 1.判断parcel 是否存在列表里；如果存在，继续执行以下拣货操作，否则提示错误并重新扫描；
     * 2.获取stationInfo;
     * 3.通过stationInfo，获取boxNum，判断BoxNum是否存在，如果存在就继续以下操作，不存在就提示station没有绑定Box，重新扫描；
     * 4.通过ParcelList请求接口上传并且保存拣货数据；
     *
     * @param code
     */
    @Override
    protected void ScanParcel(String code) {
        TParcelStationInfo area = getStationInfoByParcel(code); // null?
        if (area == null) {//位置信息没找到
            ToastUtil.showToast(getString(R.string.cannot_find_station_info));
            tvParcelNumbs.setText(getString(R.string.cannot_find_station_info));
            mpFailed.start();
            restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
        } else {
            String boxNum = getBoxNumByStationInfo(area); // null?
            if (boxNum != null) {//Parcel所对应的Station已经绑定了box，可以添加到box中了；
                //请求接口上传绑定数据；
                saveParcelBox(code, boxNum, false);
            } else {
                ToastUtil.showLongToast(getString(R.string.the_parcel_station_has_not_bean_bound_with_boxnum));
                tvParcelNumbs.setText(getString(R.string.the_parcel_station_has_not_bean_bound_with_boxnum));
                mpFailed.start();
                restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
            }
        }
    }

    /**
     * 获取Parcel的StationInfo
     *
     * @param code
     * @return
     */
    private TParcelStationInfo getStationInfoByParcel(String code) {
        for (TSubPackage subPackage : packages) {
            if (subPackage.parcelNum.equals(code)) {
                return subPackage.station;
            }
        }
        return null;
    }

    /**
     * 判断对应的Station是否已经绑定了box
     *
     * @param stationInfo
     * @return 如果返回null 说明没有绑定box
     */
    private String getBoxNumByStationInfo(TParcelStationInfo stationInfo) {
        if (StationNameBoxManager.scanStationNameBoxNum != null) {
            ArrayList<String> box = StationNameBoxManager.scanStationNameBoxNum.get(stationInfo.stationName + ":" + stationInfo.sortBy);
            if (box != null && box.size() > 0) {
                return box.get(0);//获取最新绑定的box；
            }
        }
        return null;
    }


    private void getArgs() {
        BPMap = (HashMap<String, String>) getIntent().getSerializableExtra("BP");
    }

    @Override
    public void onConfirm(View view) {
        if (rbtnParcel.isChecked()) {
            setResult(RESULT_OK);
        }
        onBackPressed();
    }

    private void saveParcelBox(final String parcel, final String box, boolean isForceSave) {
        final TParcelStationInfo stationInfo = getStationInfoByParcel(parcel);
        String BP = BPMap.get(parcel);
        if (TextUtils.isEmpty(BP)) {
            ToastUtil.showToast(parcel + getString(R.string.cannot_find_bp_value));
            restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
        }
        int pkgId = PickingActivity.getPkgIdBySubPkg(packages, parcel);
        int shipmentId = PickingActivity.getShipmentIdBySubPkgNo(packages, parcel);
        saveRequest = DeliveryService.UserSavePickSubPackage(parcel, BP, box, pkgId, shipmentId, isForceSave, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    if ("".equals(response)) {
                        ToastUtil.showToast(R.string.save_succeeded);
                        scanParcels.add(parcel);
                        mpSucceeded.start();
                        tvParcelNumbs.setText(parcel + "  put in  " + stationInfo.stationName + " --- " + stationInfo.sortBy);
                        restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
                    } else {
                        showSaveFailMsg(parcel, box, response);
                        mpFailed.start();
                    }
                } else {
                    ToastUtil.showToast(R.string.Network_is_error);
                    mpFailed.start();
                    restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        if (saveRequest != null) {
            saveRequest.cancel();
            saveRequest = null;
        }
        super.onStop();
    }

    private void showSaveFailMsg(final String parcel, final String box, String response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(response).setPositiveButton(R.string.force_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveParcelBox(parcel, box, true);//强制保存
            }
        })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        restartPreviewAfterDelay(2000);
                    }
                });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        tts.shutdown();
        super.onDestroy();
    }
}
