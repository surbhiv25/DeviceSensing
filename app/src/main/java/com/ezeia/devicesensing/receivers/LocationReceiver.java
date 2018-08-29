package com.ezeia.devicesensing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.ezeia.devicesensing.utils.Functions;
import com.google.gson.JsonObject;

import static android.content.Context.LOCATION_SERVICE;

public class LocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String act = intent.getAction();
        if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
            Log.i("TAG","Location is..."+checkIfLocEnabled(context));
            JsonObject object = new JsonObject();
            object.addProperty("Location_state",checkIfLocEnabled(context));
            object.addProperty("timestamp",CommonFunctions.fetchDateInUTC());
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"Location",object.toString(),CommonFunctions.fetchDateInUTC());
        }
    }

    private boolean checkInternetConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();

        if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkIfLocEnabled(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        // getting network status
        Boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled) {
            return false;
        }else{
            return true;
        }
    }
}
