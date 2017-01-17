package com.daigou.selfstation.d2d;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.Response;
import com.daigou.selfstation.rpc.selfstation.D2DService;
import com.daigou.selfstation.rpc.selfstation.DeliveryService;
import com.daigou.selfstation.rpc.selfstation.TLocation;
import com.google.gson.Gson;

public class LocationService extends Service {

    private static final long GPS_LOOP_TIME = 10 * 1000; // 10s

    private LocationManager locationManager;

    public boolean startReport() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗

        locationManager.requestLocationUpdates(
                locationManager.getBestProvider(criteria, true), GPS_LOOP_TIME, 100, locationListener);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    TLocation tLocation = new TLocation();
                    tLocation.latitude = location.getLatitude();
                    tLocation.longitude = location.getLongitude();
                    D2DService.UserSyncLocation(tLocation, new Response.Listener<Boolean>() {
                        @Override
                        public void onResponse(Boolean response) {
                            if (response != null) {
                                Log.d("===", response.toString());
                            }
                        }
                    });
                }
            }
        }, 5000);
        return true;
    }

    public void stopReport() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        stopReport();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder(this);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            TLocation tLocation = new TLocation();
            tLocation.latitude = location.getLatitude();
            tLocation.longitude = location.getLongitude();
            D2DService.UserSyncLocation(tLocation, new Response.Listener<Boolean>() {
                @Override
                public void onResponse(Boolean response) {
                    // not care result
                }
            });
            Log.d("===", new Gson().toJson(tLocation));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public static class ServiceBinder extends Binder {
        public final LocationService service;

        ServiceBinder(LocationService service) {
            this.service = service;
        }
    }
}
