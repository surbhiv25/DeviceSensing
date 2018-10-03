package com.ezeia.devicesensing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.google.gson.JsonObject;

import io.fabric.sdk.android.Fabric;

import static android.content.Context.LOCATION_SERVICE;

public class LocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Fabric.with(context, new Crashlytics());
        if(intent.getAction() != null)
            if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                Log.i(ForegroundService.LOG_TAG,"Location is..."+checkIfLocEnabled(context));

                if(!checkIfLocEnabled(context)){
                    Preference.getInstance(context).put(Preference.Key.LOC_LATITUDE,"0.0");
                    Preference.getInstance(context).put(Preference.Key.LOC_LONGITUDE,"0.0");
                    Toast.makeText(context,"Location OFF",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context,"Location ON",Toast.LENGTH_SHORT).show();
                }
                JsonObject object = new JsonObject();
                object.addProperty("Location_state",checkIfLocEnabled(context));
                object.addProperty("timestamp",CommonFunctions.fetchDateInUTC());
                DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"Location",object.toString(),CommonFunctions.fetchDateInUTC());
            }
    }



    public static boolean checkIfLocEnabled(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        Boolean isNetworkEnabled = false;
        // getting network status
        //Boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(locationManager != null)
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isNetworkEnabled;
    }
}
