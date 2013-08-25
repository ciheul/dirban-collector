package com.ciheul.dirbancollector.lib;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class Geolocator implements LocationListener {

    private final Context mContext;
    private Location lastKnownLocation;
    private LocationManager locationManager;

    private final static long MIN_TIME = 0;
    private final static float MIN_DISTANCE = 0;
    
    private boolean isGpsEnabled = false;
    private boolean isNetworkEnabled = false;

    private double latitude;
    private double longitude;

    public Geolocator(Context context) {
        mContext = context;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        String locationProvider = LocationManager.GPS_PROVIDER;
        locationManager.requestLocationUpdates(locationProvider, MIN_TIME, MIN_DISTANCE, this);

        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        longitude = lastKnownLocation.getLongitude();
        latitude = lastKnownLocation.getLatitude();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isGpsEnabled() {
        return isGpsEnabled;
    }

    public boolean isNetworkEnabled() {
        return isNetworkEnabled;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}
