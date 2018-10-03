package com.ezeia.devicesensing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.ezeia.devicesensing.utils.Functions;
import com.google.gson.JsonObject;

import io.fabric.sdk.android.Fabric;

public class PowerReceiver extends BroadcastReceiver {

    private Context ctx;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Fabric.with(context, new Crashlytics());
        int state = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        ctx = context;
        switch(state)
        {
            case BatteryManager.BATTERY_PLUGGED_USB:
                if(Preference.getInstance(context) != null)
                {
                    //checks if it is to be saved first time or not.
                    Boolean checkIfPluggedIn = Preference.getInstance(context).isBatteryPlugged();
                    if(checkIfPluggedIn)
                    {
                        Preference.getInstance(context).put(Preference.Key.BATTERY_PLUGGED, "USB");

                        createJson("USB");

                        //Log.i(ForegroundService.LOG_TAG,"BATTERY PLUGGED IN USB: "+ CommonFunctions.fetchDateInUTC());
                        Toast.makeText(context, "Battery Plugged In USB", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String pluggedInState = Preference.getInstance(context).getBatteryPlugStatus();
                        if(pluggedInState.equals("AC"))
                        {
                            Preference.getInstance(context).put(Preference.Key.BATTERY_PLUGGED, "USB");

                            createJson("USB");

                            //Log.i(ForegroundService.LOG_TAG,"BATTERY PLUGGED IN USB: "+ CommonFunctions.fetchDateInUTC());
                            Toast.makeText(context, "Battery Plugged In USB", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;

            case BatteryManager.BATTERY_PLUGGED_AC:
                if(Preference.getInstance(context) != null)
                {
                    Boolean checkIfPluggedIn = Preference.getInstance(context).isBatteryPlugged();
                    if(checkIfPluggedIn)
                    {
                        Preference.getInstance(context).put(Preference.Key.BATTERY_PLUGGED, "AC");

                        createJson("AC");

                        //Log.i(ForegroundService.LOG_TAG,"BATTERY PLUGGED IN AC: "+ CommonFunctions.fetchDateInUTC());
                        Toast.makeText(context, "Battery Plugged In AC", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String pluggedInState = Preference.getInstance(context).getBatteryPlugStatus();
                        if(pluggedInState.equals("USB"))
                        {
                            Preference.getInstance(context).put(Preference.Key.BATTERY_PLUGGED, "AC");

                            createJson("AC");

                            //Log.i(ForegroundService.LOG_TAG,"BATTERY PLUGGED IN AC: "+ CommonFunctions.fetchDateInUTC());
                            Toast.makeText(context, "Battery Plugged In AC", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    private void createJson(String pluggedState){
        JsonObject object = new JsonObject();
        object.addProperty("state",pluggedState);
        object.addProperty("timestamp",CommonFunctions.fetchDateInUTC());

        Functions functions = new Functions(ctx);
        JsonObject objectLoc = functions.fetchLocation();
        object.add("location",objectLoc);
        Log.i(ForegroundService.LOG_TAG, "Location is..."+objectLoc.toString());
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"BatteryPlug",object.toString(),CommonFunctions.fetchDateInUTC());
    }

}