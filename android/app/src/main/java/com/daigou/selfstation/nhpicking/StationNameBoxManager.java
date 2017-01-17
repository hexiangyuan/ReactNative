package com.daigou.selfstation.nhpicking;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by undownding on 2016/7/26.
 */
public class StationNameBoxManager {
    /**
     * key : stationName:A   stationName + ":" + sortBy
     * value: ArrayList<String> boxes
     */
    public static HashMap<String, ArrayList<String>> scanStationNameBoxNum = new HashMap<>();
}
