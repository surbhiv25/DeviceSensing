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

public class UninstallAppReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Fabric.with(context, new Crashlytics());
        String act = intent.getAction();
        if (Intent.ACTION_PACKAGE_REMOVED.equals(act)) {

            if(intent.getData() != null){
                String packageName = intent.getData().getSchemeSpecificPart();
                //Log.i(ForegroundService.LOG_TAG,"UNINSTALL APPLICATION PACKAGE: "+ packageName);
                //Log.i(ForegroundService.LOG_TAG,"UNINSTALL APPLICATION: "+ CommonFunctions.fetchDateInUTC());
                Toast.makeText(context,"Uninstall App", Toast.LENGTH_SHORT).show();

                /*Functions functions = new Functions(context);
                JsonObject objectLoc = functions.fetchLocation();
                Log.i(ForegroundService.LOG_TAG, "Location is..."+objectLoc.toString());
*/
                JsonObject object = new JsonObject();
                object.addProperty("packageName",packageName);
                object.addProperty("timestamp",CommonFunctions.fetchDateInUTC());
                //object.add("location",objectLoc);
                DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"Uninstall",object.toString(),CommonFunctions.fetchDateInUTC());
            }
        }
    }
}
