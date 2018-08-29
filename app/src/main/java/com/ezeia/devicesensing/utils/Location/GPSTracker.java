package com.ezeia.devicesensing.utils.Location;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.ezeia.devicesensing.utils.CommonFunctions;


public class GPSTracker extends Service implements LocationListener {

    private Context mContext = null;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    public double getAltitude() {
        if(location != null){
            altitude = location.getAltitude();
        }
        return altitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getSpeed() {
        if(location != null){
            speed = location.getSpeed();
        }
        return speed;
    }

    public double getBearing() {
        if(location != null){
            bearing = location.getBearing();
        }
        return bearing;
    }

    public boolean isHasAccuracy() {
        if(location != null){
            hasAccuracy = location.hasAccuracy();
        }
        return hasAccuracy;
    }

    public boolean isHasSpeed() {
        if(location != null){
            hasSpeed = location.hasSpeed();
        }
        return hasSpeed;
    }


    public boolean isHasAltitude() {
        if(location != null){
            hasAltitude = location.hasAltitude();
        }
        return hasAltitude;
    }

    public boolean isHasBearing() {
        if(location != null){
            hasBearing = location.hasBearing();
        }
        return hasBearing;
    }

    public boolean isFromMockProvider() {
        if(location != null){
            isFromMockProvider = location.isFromMockProvider();
        }
        return isFromMockProvider;
    }

    public String getProvider() {
        if(location != null){
            provider = location.getProvider();
        }
        return provider;
    }

    public double getElapsedTime() {
        if(location != null){
            elapsedTime = location.getElapsedRealtimeNanos();
        }
        return elapsedTime;
    }

    double altitude;
    double accuracy;
    double speed;
    double bearing;
    boolean hasAccuracy;
    boolean hasSpeed;
    boolean hasAltitude;
    boolean hasBearing;
    boolean isFromMockProvider;
    String provider;
    double elapsedTime;
    String timestamp;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1000; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES =  100000 * 60 * 1; // 5 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    public GPSTracker() {
    }

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            //isGPSEnabled = locationManager
            //.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                //showSettingsAlert();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, GPSTracker.this);

                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            accuracy = location.getAccuracy();
                            altitude = location.getAltitude();
                            speed = location.getSpeed();
                            bearing = location.getBearing();
                            hasAccuracy = location.hasAccuracy();
                            hasAltitude = location.hasAltitude();
                            hasBearing = location.hasBearing();
                            hasSpeed = location.hasSpeed();
                            isFromMockProvider = location.isFromMockProvider();
                            provider = location.getProvider();
                            elapsedTime = location.getElapsedRealtimeNanos();
                            timestamp = CommonFunctions.fetchDateInUTC();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                else {
                    if(isGPSEnabled) {

                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    accuracy = location.getAccuracy();
                                    altitude = location.getAltitude();
                                    speed = location.getSpeed();
                                    bearing = location.getBearing();
                                    hasAccuracy = location.hasAccuracy();
                                    hasAltitude = location.hasAltitude();
                                    hasBearing = location.hasBearing();
                                    hasSpeed = location.hasSpeed();
                                    isFromMockProvider = location.isFromMockProvider();
                                    provider = location.getProvider();
                                    elapsedTime = location.getElapsedRealtimeNanos();
                                    timestamp = CommonFunctions.fetchDateInUTC();
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }





    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                System.exit(0);

            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}