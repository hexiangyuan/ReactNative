package com.daigou.selfstation.system;


import com.daigou.selfstation.utils.CountryInfo;

/**
 * Created by ：wangxiang on 15/10/9
 */
public class AppUrl {

    private static boolean isLiving = true;//判断是线上还是线下；

    public static boolean isLiving() {
        return isLiving;
    }

    public static void setIsLiving(boolean isLiving) {
        AppUrl.isLiving = isLiving;
    }

    /**
     * debug
     */
    public static String kJsonRpcCoreUrl = !isLiving ? "http://delivery.65emall.net/api/" : "http://pdt.65daigou.com/api/";
    /**
     * product
     */
//    public static String kJsonRpcCoreUrl = "http://pdt.65daigou.com/api/";
    /**
     * partner Shop Url
     */
    private static String PARTNER_SHOP_URL = "http://ezdelivery.65daigou.com/";

    /**
     * domain
     */
    private static String DOMAIN = ".65daigou.com";

    public static String getPartnerShopUrl() {
        return PARTNER_SHOP_URL = !isLiving ?
                "http://delivery.65emall.net/commisions/index.html?country=" + CountryInfo.getCountrySign()
                : "http://pdt.65daigou.com/commisions/index.html?country=" + CountryInfo.getCountrySign();

    }

    public static void setPartnerShopUrl(String partnerShopUrl) {
        PARTNER_SHOP_URL = partnerShopUrl;
    }

    public static String getDOMAIN() {
        return DOMAIN = !isLiving ? ".65emall.net" : ".65daigou.com";
    }

    public static void setDOMAIN(String DOMAIN) {
        AppUrl.DOMAIN = DOMAIN;
    }
}
