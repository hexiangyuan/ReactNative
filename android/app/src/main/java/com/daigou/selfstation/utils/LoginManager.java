package com.daigou.selfstation.utils;

import com.daigou.selfstation.rpc.selfstation.TLoginResult;
import com.daigou.selfstation.system.EzDeliveryApplication;

/**
 * Created by 何祥源 on 16/6/28.
 * Desc:登陆时候用户的信息；
 */
public class LoginManager {
    public static String DELIVERY_STAFF = "delivery staff";
    public static String PARTNER_SHOP = "partner shop";
    public static String DRIVER = "driver";
    public static String PICKER = "picker";

    /**
     * 获取用户类型；
     *
     * @return delivery——staff
     */
    public static String getUserType() {
        String userType = DELIVERY_STAFF;
        TLoginResult loginResult = EzDeliveryApplication.getInstance().getLoginResult();
        if (loginResult != null) {
            userType = loginResult.userType;
        }
        return userType;
    }

    public static boolean isPartTime() {
        TLoginResult loginResult = EzDeliveryApplication.getInstance().getLoginResult();
        return loginResult.isPartTime;
    }

    public static boolean isDeliveryStaff() {
        return DELIVERY_STAFF.equals(getUserType());
    }

    public static boolean isPartnerShop() {
        return PARTNER_SHOP.equals(getUserType());
    }

    public static boolean isDriver() {
        return DRIVER.equals(getUserType());
    }

    public static boolean isPicker() {
        return PICKER.equals(getUserType());
    }
}
