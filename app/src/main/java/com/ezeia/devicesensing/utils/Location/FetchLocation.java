package com.ezeia.devicesensing.utils.Location;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.service.ForegroundService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class FetchLocation implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final GoogleApiClient mGoogleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private Location mLocation;
    private final Context ctx;
    private double latitude;
    private double longitude;

    public FetchLocation(Context ctx) {

        this.ctx = ctx;

        mGoogleApiClient = new GoogleApiClient.Builder(ctx)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if(!mGoogleApiClient.isConnected())
                mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try{
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }catch(SecurityException e){
            e.printStackTrace();
        }

        if(mLocation!=null)
        {
            latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();
        }

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onLocationChanged(Location location) {
        if(location!=null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            double accuracy = location.getAccuracy();
            double altitude = location.getAltitude();
            double speed = location.getSpeed();
            double bearing = location.getBearing();
            boolean hasAccuracy = location.hasAccuracy();
            boolean hasAltitude = location.hasAltitude();
            boolean hasSpeed = location.hasSpeed();
            boolean hasBearing = location.hasBearing();
            boolean isFromMockProvider = location.isFromMockProvider();
            String provider = location.getProvider();
            double elapsedTime = location.getElapsedRealtimeNanos();

            //Log.i(ForegroundService.LOG_TAG,"LOCATION IS..."+latitude+"---"+longitude);
            if(Preference.getInstance(ctx) != null){
                Preference.getInstance(ctx).put(Preference.Key.LOC_LATITUDE,latitude);
                Preference.getInstance(ctx).put(Preference.Key.LOC_LONGITUDE,longitude);
                Preference.getInstance(ctx).put(Preference.Key.LOC_ALTITUDE, altitude);
                Preference.getInstance(ctx).put(Preference.Key.LOC_ACCURACY, accuracy);
                Preference.getInstance(ctx).put(Preference.Key.LOC_SPEED, speed);
                Preference.getInstance(ctx).put(Preference.Key.LOC_BEARING, bearing);
                Preference.getInstance(ctx).put(Preference.Key.LOC_HAS_ACCURACY, hasAccuracy);
                Preference.getInstance(ctx).put(Preference.Key.LOC_HAS_ALTITUDE, hasAltitude);
                Preference.getInstance(ctx).put(Preference.Key.LOC_HAS_SPEED, hasSpeed);
                Preference.getInstance(ctx).put(Preference.Key.LOC_HAS_BEARING, hasBearing);
                Preference.getInstance(ctx).put(Preference.Key.LOC_MOCK_PROVIDER, isFromMockProvider);
                Preference.getInstance(ctx).put(Preference.Key.LOC_PROVIDER, provider);
                Preference.getInstance(ctx).put(Preference.Key.LOC_ELAPSED_TIME, elapsedTime);
            }
        }
    }

   /* private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(ctx);
        return resultCode != ConnectionResult.SUCCESS;
    }*/

    private void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        long UPDATE_INTERVAL = 5000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        long FASTEST_INTERVAL = 5000;
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(ctx.getApplicationContext(), "Enable Permissions", Toast.LENGTH_LONG).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    public void stopLocationUpdates()
    {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

}
