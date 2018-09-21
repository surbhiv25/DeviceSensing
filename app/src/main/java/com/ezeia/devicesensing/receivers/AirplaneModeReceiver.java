package com.ezeia.devicesensing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.ezeia.devicesensing.utils.Functions;
import com.google.gson.JsonObject;

import io.fabric.sdk.android.Fabric;

public class AirplaneModeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Fabric.with(context, new Crashlytics());
        boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
        if(isAirplaneModeOn){
            Toast.makeText(context,"Airplane mode ON",Toast.LENGTH_SHORT).show();
           // Log.i(ForegroundService.LOG_TAG,"AIRPLANE MODE ON: "+ CommonFunctions.fetchDateInUTC());
            JsonObject object = new JsonObject();
            object.addProperty("state","ON");
            object.addProperty("timestamp",CommonFunctions.fetchDateInUTC());

            Functions functions = new Functions(context);
            JsonObject objectLoc = functions.fetchLocation();
            object.add("location",objectLoc);
            Log.i("LOCATION", "Location is..."+objectLoc.toString());
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"Airplane",object.toString(),CommonFunctions.fetchDateInUTC());

        } else {
            Toast.makeText(context,"Airplane mode OFF",Toast.LENGTH_SHORT).show();
            Log.i(ForegroundService.LOG_TAG,"AIRPLANE MODE OFF: "+ CommonFunctions.fetchDateInUTC());

            JsonObject object = new JsonObject();
            object.addProperty("state","OFF");
            object.addProperty("timestamp",CommonFunctions.fetchDateInUTC());

            Functions functions = new Functions(context);
            JsonObject objectLoc = functions.fetchLocation();
            object.add("location",objectLoc);
            Log.i("LOCATION", "Location is..."+objectLoc.toString());
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"Airplane",object.toString(),CommonFunctions.fetchDateInUTC());
        }
    }

}
