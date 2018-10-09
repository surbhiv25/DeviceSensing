package com.ezeia.devicesensing.receivers;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.fabric.sdk.android.Fabric;

public class WifiReceiver extends BroadcastReceiver {
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Fabric.with(context, new Crashlytics());
        this.context = context;
        int WifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

        switch (WifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                //Log.i(ForegroundService.LOG_TAG,"WIFI ON: "+ CommonFunctions.fetchDateInUTC());
                getConnectedWifi();
                //Log.i(ForegroundService.LOG_TAG,"CONNECTED WIFI INFO: "+getConnectedWifi("ON"));
                Toast.makeText(context,"Wifi ON", Toast.LENGTH_SHORT).show();
                break;

            case WifiManager.WIFI_STATE_ENABLING:
                //Toast.makeText(context,"wifi enabling", Toast.LENGTH_SHORT).show();
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                //Log.i(ForegroundService.LOG_TAG,"WIFI OFF: "+ CommonFunctions.fetchDateInUTC());
                //Log.i(ForegroundService.LOG_TAG,"CONNECTED WIFI INFO: "+getConnectedWifi());
                Toast.makeText(context,"Wifi OFF", Toast.LENGTH_SHORT).show();

                createJson();

                break;

            case WifiManager.WIFI_STATE_DISABLING:
                //Toast.makeText(context,"wifi disabling", Toast.LENGTH_SHORT).show();
                break;

            case WifiManager.WIFI_STATE_UNKNOWN:
                //Toast.makeText(context,"wifi unknown", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void getConnectedWifi()
    {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;
        JsonArray array = null;
        JsonObject object = null;
        if(manager != null)
        {
            wifiInfo = manager.getConnectionInfo();
            if(wifiInfo != null)
            {
                object = new JsonObject();
                array = new JsonArray();
                object.addProperty("BSSID",wifiInfo.getBSSID());
                object.addProperty("SSID",wifiInfo.getSSID());
                object.addProperty("Supplicant State",String.valueOf(wifiInfo.getSupplicantState()));
                object.addProperty("RSSI",wifiInfo.getRssi());
                object.addProperty("Mac Address",wifiInfo.getMacAddress());
                object.addProperty("Link Speed",wifiInfo.getLinkSpeed());
                object.addProperty("Frequency",wifiInfo.getFrequency());
                object.addProperty("Network ID",wifiInfo.getNetworkId());
                object.addProperty("Hidden SSID",wifiInfo.getHiddenSSID());
                array.add(object);
            }
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("state","ON");
        jsonObject.addProperty("timestamp",CommonFunctions.fetchDateInUTC());
        jsonObject.add("connection",array);

       /* Functions functions = new Functions(context);
        JsonObject objectLoc = functions.fetchLocation();
        jsonObject.add("location",objectLoc);
        Log.i(ForegroundService.LOG_TAG, "Location is..."+objectLoc.toString());*/

        if(object != null)
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"WifiConnection",object.toString(),CommonFunctions.fetchDateInUTC());
        else
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"WifiConnection","NA",CommonFunctions.fetchDateInUTC());
    }

    private void createJson(){

        /*Functions functions = new Functions(context);
        JsonObject objectLoc = functions.fetchLocation();
        Log.i(ForegroundService.LOG_TAG, "Location is..."+objectLoc.toString());
*/
        JsonObject object = new JsonObject();
        object.addProperty("state","OFF");
        object.addProperty("timestamp",CommonFunctions.fetchDateInUTC());
        //object.add("location",objectLoc);
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"WifiConnection",object.toString(),CommonFunctions.fetchDateInUTC());
    }

}