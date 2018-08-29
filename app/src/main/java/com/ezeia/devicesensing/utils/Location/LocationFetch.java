package com.ezeia.devicesensing.utils.Location;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.an.deviceinfo.permission.PermissionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationFetch implements LocationListener
{
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 0;

    private final Context context;
    private final PermissionUtils permissionUtils;
    public LocationFetch(Context context) {
        this.context = context;
        this.permissionUtils = new PermissionUtils(context);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressWarnings("MissingPermission")
    private ArrayList<Object> getLocationLatLong() {
        ArrayList<Object> latlong = new ArrayList<>();
        Double accuracy,bearing,speed, latitude, longitude;
        Double altitude, getExtra= 0.0;
        String getProvider;
        Long getElaspedTime,getTime;
        Boolean hasSpeed,hasAccuracy,hasAltitude,hasBearing,isFromMock;

        StringBuilder buffer = new StringBuilder();
        try {
            // Get the location manager
            LocationManager Locationm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // ——–Gps provider—
            Locationm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this, Looper.getMainLooper());

            Location locationGPS = Locationm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null)
            {
                latitude = locationGPS.getLatitude();
                longitude = locationGPS.getLongitude();
                accuracy = (double) locationGPS.getAccuracy();
                altitude = locationGPS.getAltitude();
                bearing = (double) locationGPS.getBearing();
                speed = (double) locationGPS.getSpeed();
                hasSpeed = locationGPS.hasSpeed();
                hasAccuracy = locationGPS.hasAccuracy();
                hasAltitude = locationGPS.hasAltitude();
                hasBearing = locationGPS.hasBearing();
                isFromMock = locationGPS.isFromMockProvider();
                getElaspedTime = locationGPS.getElapsedRealtimeNanos();
                //locationGPS.getExtras();
                getProvider = locationGPS.getProvider();
                getTime = locationGPS.getTime();

                latlong.add(latitude);
                latlong.add(longitude);
                latlong.add(accuracy);
                latlong.add(altitude);
                latlong.add(bearing);
                latlong.add(speed);
                latlong.add(hasSpeed);
                latlong.add(hasAccuracy);
                latlong.add(hasAltitude);
                latlong.add(hasBearing);
                latlong.add(isFromMock);
                latlong.add(getElaspedTime);
                latlong.add(getProvider);
                latlong.add(getTime);

                Locationm.removeUpdates(this);
                return latlong;
            }

            // ——–Network provider—
            Locationm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this, Looper.getMainLooper());
            Location locationNet = Locationm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (locationNet != null) {
                latitude = locationNet.getLatitude();
                longitude = locationNet.getLongitude();
                accuracy = (double) locationNet.getAccuracy();
                altitude = locationNet.getAltitude();
                bearing = (double) locationNet.getBearing();
                speed = (double) locationNet.getSpeed();
                hasSpeed = locationNet.hasSpeed();
                hasAccuracy = locationNet.hasAccuracy();
                hasAltitude = locationNet.hasAltitude();
                hasBearing = locationNet.hasBearing();
                isFromMock = locationNet.isFromMockProvider();
                getElaspedTime = locationNet.getElapsedRealtimeNanos();
                //locationNet.getExtras();
                getProvider = locationNet.getProvider();
                getTime = locationNet.getTime();

                latlong.add(latitude);
                latlong.add(longitude);
                latlong.add(accuracy);
                latlong.add(altitude);
                latlong.add(bearing);
                latlong.add(speed);
                latlong.add(hasSpeed);
                latlong.add(hasAccuracy);
                latlong.add(hasAltitude);
                latlong.add(hasBearing);
                latlong.add(isFromMock);
                latlong.add(getElaspedTime);
                latlong.add(getProvider);
                latlong.add(getTime);

                Locationm.removeUpdates(this);
                return latlong;
            }

            Location locationPassive = Locationm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (locationPassive != null) {
                latitude = locationPassive.getLatitude();
                longitude = locationPassive.getLongitude();
                accuracy = (double) locationPassive.getAccuracy();
                altitude = locationPassive.getAltitude();
                bearing = (double) locationPassive.getBearing();
                speed = (double) locationPassive.getSpeed();
                hasSpeed = locationPassive.hasSpeed();
                hasAccuracy = locationPassive.hasAccuracy();
                hasAltitude = locationPassive.hasAltitude();
                hasBearing = locationPassive.hasBearing();
                isFromMock = locationPassive.isFromMockProvider();
                getElaspedTime = locationPassive.getElapsedRealtimeNanos();
                //locationPassive.getExtras();
                getProvider = locationPassive.getProvider();
                getTime = locationPassive.getTime();

                latlong.add(latitude);
                latlong.add(longitude);
                latlong.add(accuracy);
                latlong.add(altitude);
                latlong.add(bearing);
                latlong.add(speed);
                latlong.add(hasSpeed);
                latlong.add(hasAccuracy);
                latlong.add(hasAltitude);
                latlong.add(hasBearing);
                latlong.add(isFromMock);
                latlong.add(getElaspedTime);
                latlong.add(getProvider);
                latlong.add(getTime);

                Locationm.removeUpdates(this);
                return latlong;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return latlong;
    }


    private DeviceLoc getAddressFromLocation(ArrayList<Object> listOfVal) {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        DeviceLoc addressInfo = new DeviceLoc();
        try {
            Double latitude= (Double) listOfVal.get(0);
            Double longitude= (Double) listOfVal.get(1);
            Double accuracy= (Double) listOfVal.get(2);
            Double altitude= (Double) listOfVal.get(3);
            Double bearing= (Double) listOfVal.get(4);
            Double speed= (Double) listOfVal.get(5);

            Boolean hasSpeed= (Boolean) listOfVal.get(6);
            Boolean hasAccuracy= (Boolean) listOfVal.get(7);
            Boolean hasAltitude= (Boolean) listOfVal.get(8);
            Boolean hasBearing= (Boolean) listOfVal.get(9);
            Boolean isFromMock= (Boolean) listOfVal.get(10);

            Long getElaspedTime= (Long) listOfVal.get(11);
            String getProvider= (String) listOfVal.get(12);
            Long getTime= (Long) listOfVal.get(13);

            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);

            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                addressInfo.setAddressLine1(address.getAddressLine(0));
                addressInfo.setCity(address.getLocality());
                addressInfo.setPostalCode(address.getPostalCode());
                addressInfo.setState(address.getAdminArea());
                addressInfo.setCountryCode(address.getCountryCode());
                addressInfo.setLatitude(latitude);
                addressInfo.setLongitude(longitude);
                addressInfo.setAccuracy(accuracy);
                addressInfo.setAltitude(altitude);
                addressInfo.setBearing(bearing);
                addressInfo.setSpeed(speed);

                addressInfo.setHasSpeed(hasSpeed);
                addressInfo.setHasAccuracy(hasAccuracy);
                addressInfo.setHasAltitude(hasAltitude);
                addressInfo.setHasBearing(hasBearing);
                addressInfo.setElaspedTime(getElaspedTime);
                addressInfo.setProvider(getProvider);
                addressInfo.setTime(getTime);

                return addressInfo;
            }
        } catch (Exception e) {
            Log.e("", "Unable connect to Geocoder", e);
        } finally {
            return addressInfo;
        }
    }

    public DeviceLoc getLocation() {
        if(!permissionUtils.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION))
            throw new RuntimeException("Access Fine Location permission not granted!");

        ArrayList<Object> latlong = getLocationLatLong();
        return getAddressFromLocation(latlong);
    }

    @Override
    public void onLocationChanged(Location location) {

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
}
