package com.ezeia.devicesensing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.google.gson.JsonObject;

import io.fabric.sdk.android.Fabric;

public class InstallAppReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Fabric.with(context, new Crashlytics());
        String act = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(act)) {

            String packageName = "";
            if(intent.getData() != null)
                packageName = intent.getData().getSchemeSpecificPart();
            Toast.makeText(context,"Install App "+packageName, Toast.LENGTH_SHORT).show();

            JsonObject object = new JsonObject();
            object.addProperty("packageName",packageName);
            object.addProperty("timestamp",CommonFunctions.fetchDateInUTC());

            /*
            Functions functions = new Functions(context);
            JsonObject objectLoc = functions.fetchLocation();
            object.add("location",objectLoc);
            Log.i(ForegroundService.LOG_TAG, "Location is..."+objectLoc.toString());*/

            DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"Install",object.toString(),CommonFunctions.fetchDateInUTC());
        }
    }

}
