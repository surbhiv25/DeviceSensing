package com.ezeia.devicesensing.utils.Location;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.ezeia.devicesensing.pref.Preference;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class FetchLocation implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    GoogleApiClient mGoogleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private Location mLocation;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 5000;  /* 15 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private Context ctx;
    public double latitude, longitude,altitude,accuracy,speed,bearing,elapsedTime;
    public boolean hasAccuracy,hasAltitude,hasSpeed,hasBearing,isFromMockProvider;
    public String provider;

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

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            accuracy = location.getAccuracy();
            altitude = location.getAltitude();
            speed = location.getSpeed();
            bearing = location.getBearing();
            hasAccuracy = location.hasAccuracy();
            hasAltitude = location.hasAltitude();
            hasSpeed = location.hasSpeed();
            hasBearing = location.hasBearing();
            isFromMockProvider = location.isFromMockProvider();
            provider = location.getProvider();
            elapsedTime = location.getElapsedRealtimeNanos();

            Log.i("TAG","LOCATION IS..."+latitude+"---"+longitude);
            if(Preference.getInstance(ctx) != null){
                Preference.getInstance(ctx).put(Preference.Key.LOC_LATITUDE,latitude);
                Preference.getInstance(ctx).put(Preference.Key.LOC_LONGITUDE,longitude);
                Preference.getInstance(ctx).put(Preference.Key.LOC_ALTITUDE,altitude);
                Preference.getInstance(ctx).put(Preference.Key.LOC_ACCURACY,accuracy);
                Preference.getInstance(ctx).put(Preference.Key.LOC_SPEED,speed);
                Preference.getInstance(ctx).put(Preference.Key.LOC_BEARING,bearing);
                Preference.getInstance(ctx).put(Preference.Key.LOC_HAS_ACCURACY,hasAccuracy);
                Preference.getInstance(ctx).put(Preference.Key.LOC_HAS_ALTITUDE,hasAltitude);
                Preference.getInstance(ctx).put(Preference.Key.LOC_HAS_SPEED,hasSpeed);
                Preference.getInstance(ctx).put(Preference.Key.LOC_HAS_BEARING,hasBearing);
                Preference.getInstance(ctx).put(Preference.Key.LOC_MOCK_PROVIDER,isFromMockProvider);
                Preference.getInstance(ctx).put(Preference.Key.LOC_PROVIDER,provider);
                Preference.getInstance(ctx).put(Preference.Key.LOC_ELAPSED_TIME,elapsedTime);
            }
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(ctx);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                //apiAvailability.getErrorDialog(ctx., resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else
               // ctx.finish();

            return false;
        }
        return true;
    }

    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
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
