package com.ciheul.dirbancollector.lib;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class Geolocator implements LocationListener {

    private final Context m_context;
    private Location last_known_location;
    private LocationManager location_manager;

    private final static long MIN_TIME = 0;
    private final static float MIN_DISTANCE = 0;
    
    private boolean is_gps_enabled = false;
    private boolean is_network_enabled = false;

    private double latitude;
    private double longitude;

    public Geolocator(Context context) {
        m_context = context;
        location_manager = (LocationManager) m_context.getSystemService(Context.LOCATION_SERVICE);

        is_gps_enabled = location_manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        is_network_enabled = location_manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        String location_provider = LocationManager.GPS_PROVIDER;
        location_manager.requestLocationUpdates(location_provider, MIN_TIME, MIN_DISTANCE, this);

        last_known_location = location_manager.getLastKnownLocation(location_provider);
        longitude = last_known_location.getLongitude();
        latitude = last_known_location.getLatitude();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isGPSEnabled() {
        return is_gps_enabled;
    }

    public boolean isNetworkEnabled() {
        return is_network_enabled;
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
