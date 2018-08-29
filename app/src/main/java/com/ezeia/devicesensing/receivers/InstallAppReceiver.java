package com.ezeia.devicesensing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.ezeia.devicesensing.utils.Functions;
import com.google.gson.JsonObject;

public class InstallAppReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String act = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(act)) {

            String packageName = intent.getData().getSchemeSpecificPart();
            //Log.i(ForegroundService.LOG_TAG,"INSTALL APPLICATION PACKAGE: "+ packageName);
            //Log.i(ForegroundService.LOG_TAG,"INSTALL APPLICATION: "+ CommonFunctions.fetchDateInUTC());
            //Toast.makeText(context,"Install App", Toast.LENGTH_SHORT).show();

            Functions functions = new Functions(context);
            JsonObject objectLoc = functions.fetchLocation();
            JsonObject object = new JsonObject();
            object.addProperty("packageName",packageName);
            object.addProperty("timestamp",CommonFunctions.fetchDateInUTC());
            object.add("location",objectLoc);

            DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"Install",object.toString(),CommonFunctions.fetchDateInUTC());
        }
    }

}
