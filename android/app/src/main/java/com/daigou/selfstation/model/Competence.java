package com.daigou.selfstation.model;

import com.daigou.selfstation.R;
import com.daigou.selfstation.utils.CountryInfo;
import com.daigou.selfstation.utils.LoginManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxiang on 2016/11/12.
 */

public class Competence {
    public static List<Function> DATA;

    public static void setData() {
        DATA = new ArrayList<>();
        if (CountryInfo.isSingapore()) {
            if (LoginManager.isDeliveryStaff()) {
                DATA.add(new Function(R.drawable.ic_scan, R.string.scan_to_shelf));
                DATA.add(new Function(R.drawable.ic_picking, R.string.picking));
                DATA.add(new Function(R.drawable.ic_search, R.string.search_parcel));
                DATA.add(new Function(R.drawable.ic_search_scan, R.string.scan_search));
                DATA.add(new Function(R.drawable.ic_ready_for_collection, R.string.ready_for_collection));
                DATA.add(new Function(R.drawable.ic_delivery, R.string.out_for_delivery));
                DATA.add(new Function(R.drawable.ic_neighborhood_picking, R.string.neighborhood_picking));
                DATA.add(new Function(R.drawable.ic_d2d, R.string.d2d));
                DATA.add(new Function(R.drawable.ic_driver_pack, R.string.packing));
            } else if (LoginManager.isPicker()) {
                DATA.add(new Function(R.drawable.ic_scan, R.string.scan_to_shelf));
                DATA.add(new Function(R.drawable.ic_picking, R.string.picking));
                DATA.add(new Function(R.drawable.ic_search, R.string.search_parcel));
                DATA.add(new Function(R.drawable.ic_search_scan, R.string.scan_search));
                DATA.add(new Function(R.drawable.ic_scan_cl, R.string.scan_for_clearance));
                DATA.add(new Function(R.drawable.ic_ready_for_collection, R.string.ready_for_collection));
                DATA.add(new Function(R.drawable.ic_delivery, R.string.out_for_delivery));
                DATA.add(new Function(R.drawable.ic_neighborhood_picking, R.string.neighborhood_picking));
                DATA.add(new Function(R.drawable.ic_d2d, R.string.d2d));
                DATA.add(new Function(R.drawable.ic_driver_pack, R.string.packing));
                DATA.add(new Function(R.drawable.ic_check_in, R.string.check_in));
                DATA.add(new Function(R.drawable.ic_my_job, R.string.my_job));
            } else if (LoginManager.isDriver()) {
                DATA.add(new Function(R.drawable.ic_scan, R.string.scan_to_shelf));
                DATA.add(new Function(R.drawable.ic_picking, R.string.picking));
                DATA.add(new Function(R.drawable.ic_search, R.string.search_parcel));
                DATA.add(new Function(R.drawable.ic_search_scan, R.string.scan_search));
                DATA.add(new Function(R.drawable.ic_ready_for_collection, R.string.ready_for_collection));
                DATA.add(new Function(R.drawable.ic_delivery, R.string.out_for_delivery));
                DATA.add(new Function(R.drawable.ic_neighborhood_picking, R.string.neighborhood_picking));
                DATA.add(new Function(R.drawable.ic_d2d, R.string.d2d));
                DATA.add(new Function(R.drawable.ic_driver_pack, R.string.packing));
                DATA.add(new Function(R.drawable.ic_partner_shop, R.string.partner_shop));
            }
        } else if (CountryInfo.isMalaysia()) {
            if (LoginManager.isPartnerShop()) {
                DATA.add(new Function(R.drawable.ic_list, R.string.parcel_list));
                DATA.add(new Function(R.drawable.ic_sign, R.string.sign));
                DATA.add(new Function(R.drawable.ic_partner_shop, R.string.partner_shop));
                DATA.add(new Function(R.drawable.ic_search, R.string.search_parcel));
                DATA.add(new Function(R.drawable.ic_ready_for_collection, R.string.ready_for_collection));
            } else if (LoginManager.isDeliveryStaff()) {
                DATA.add(new Function(R.drawable.ic_scan, R.string.scan_to_shelf));
                DATA.add(new Function(R.drawable.ic_picking, R.string.picking));
                DATA.add(new Function(R.drawable.ic_list, R.string.parcel_list));
                DATA.add(new Function(R.drawable.ic_sign, R.string.sign));
                DATA.add(new Function(R.drawable.ic_search, R.string.search_parcel));
                DATA.add(new Function(R.drawable.ic_ready_for_collection, R.string.ready_for_collection));
                DATA.add(new Function(R.drawable.ic_delivery, R.string.out_for_delivery));
                DATA.add(new Function(R.drawable.ic_driver_pack, R.string.packing));
            }
        } else {
            if (LoginManager.isDeliveryStaff()) {
                DATA.add(new Function(R.drawable.ic_scan, R.string.scan_to_shelf));
                DATA.add(new Function(R.drawable.ic_picking, R.string.picking));
                DATA.add(new Function(R.drawable.ic_search, R.string.search_parcel));
                DATA.add(new Function(R.drawable.ic_search_scan, R.string.scan_search));
                DATA.add(new Function(R.drawable.ic_ready_for_collection, R.string.ready_for_collection));
                DATA.add(new Function(R.drawable.ic_delivery, R.string.out_for_delivery));
                DATA.add(new Function(R.drawable.ic_neighborhood_picking, R.string.neighborhood_picking));
                DATA.add(new Function(R.drawable.ic_d2d, R.string.d2d));
                DATA.add(new Function(R.drawable.ic_driver_pack, R.string.packing));
            } else if (LoginManager.isPicker()) {
                DATA.add(new Function(R.drawable.ic_scan, R.string.scan_to_shelf));
                DATA.add(new Function(R.drawable.ic_picking, R.string.picking));
                DATA.add(new Function(R.drawable.ic_search, R.string.search_parcel));
                DATA.add(new Function(R.drawable.ic_search_scan, R.string.scan_search));
                DATA.add(new Function(R.drawable.ic_ready_for_collection, R.string.ready_for_collection));
                DATA.add(new Function(R.drawable.ic_delivery, R.string.out_for_delivery));
                DATA.add(new Function(R.drawable.ic_neighborhood_picking, R.string.neighborhood_picking));
                DATA.add(new Function(R.drawable.ic_d2d, R.string.d2d));
                DATA.add(new Function(R.drawable.ic_driver_pack, R.string.packing));
                DATA.add(new Function(R.drawable.ic_check_in, R.string.check_in));
                DATA.add(new Function(R.drawable.ic_my_job, R.string.my_job));
            } else if (LoginManager.isDriver()) {
                DATA.add(new Function(R.drawable.ic_scan, R.string.scan_to_shelf));
                DATA.add(new Function(R.drawable.ic_picking, R.string.picking));
                DATA.add(new Function(R.drawable.ic_search, R.string.search_parcel));
                DATA.add(new Function(R.drawable.ic_search_scan, R.string.scan_search));
                DATA.add(new Function(R.drawable.ic_ready_for_collection, R.string.ready_for_collection));
                DATA.add(new Function(R.drawable.ic_delivery, R.string.out_for_delivery));
                DATA.add(new Function(R.drawable.ic_neighborhood_picking, R.string.neighborhood_picking));
                DATA.add(new Function(R.drawable.ic_d2d, R.string.d2d));
                DATA.add(new Function(R.drawable.ic_driver_pack, R.string.packing));
                DATA.add(new Function(R.drawable.ic_partner_shop, R.string.partner_shop));
            } else if (LoginManager.isPartnerShop()) {
                DATA.add(new Function(R.drawable.ic_list, R.string.parcel_list));
                DATA.add(new Function(R.drawable.ic_sign, R.string.sign));
                DATA.add(new Function(R.drawable.ic_partner_shop, R.string.partner_shop));
                DATA.add(new Function(R.drawable.ic_search, R.string.search_parcel));
                DATA.add(new Function(R.drawable.ic_ready_for_collection, R.string.ready_for_collection));
            }
        }
        DATA.add(new Function(R.drawable.ic_setting, R.string.my_setting));
        DATA.add(new Function(R.drawable.ic_update, R.string.update));
        DATA.add(new Function(R.drawable.ic_log_out, R.string.logout));
        DATA.add(new Function(R.drawable.ic_work_schedule, R.string.work_schedule));
        DATA.add(new Function(R.drawable.ic_cancel_from, R.string.cancel_form));
        DATA.add(new Function(R.drawable.ic_cancel_from, R.string.cancel_form));
        DATA.add(new Function(R.drawable.ic_scan, R.string.react_native));
    }

    public static class Function {
        private int drawableId;
        private int desc;

        private Function(int drawableId, int desc) {
            this.drawableId = drawableId;
            this.desc = desc;
        }

        public int getDrawableId() {
            return drawableId;
        }

        public int getDesc() {
            return desc;
        }
    }
}


