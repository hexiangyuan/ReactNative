package com.daigou.selfstation.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.daigou.selfstation.system.EzDeliveryApplication;

/**
 * Created by 何祥源 on 16/4/28.
 * Desc:
 */
public class CountryInfo {

    private final static String SG = "SG";
    private final static String MY = "MY";

    public static String getCountry() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(EzDeliveryApplication.getInstance());
        return sp.getString("country", "singapore");
    }

    public static String getCountrySign() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(EzDeliveryApplication.getInstance());
        return sp.getString("country_sign", "SG");
    }


    public static boolean isMalaysia() {
        return getCountry().equalsIgnoreCase(MY);
    }

    public static boolean isSingapore() {
        return getCountry().equalsIgnoreCase(SG);
    }
}
