package com.ezeia.devicesensing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.service.AwsUploader;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.ezeia.devicesensing.utils.Functions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

public class ScreenReceiver extends BroadcastReceiver{

    private Context ctx;
    private JSONObject object;

    @Override
    public void onReceive(Context context, Intent intent) {
            ctx = context;
        Fabric.with(context, new Crashlytics());
        if(intent.getAction() != null)
        {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Toast.makeText(context, "Screen OFF", Toast.LENGTH_SHORT).show();
                Preference.getInstance(context).put(Preference.Key.SCREEN_OFF_TIME,CommonFunctions.fetchDateInUTC());
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if(!isCallActive(ctx) && !isMusicPlaying(ctx)){
                            Log.i(ForegroundService.LOG_TAG,"TIME AFTER 30 SECONDS..."+CommonFunctions.fetchDateInUTC());
                            if(Preference.getInstance(ctx) != null){
                                if(!Preference.getInstance(ctx).getReportSentStatus()){
                                    startCreatingJSON(ctx);
                                }
                            }
                        }
                        else if(isCallActive(ctx)){
                            Log.i(ForegroundService.LOG_TAG,"CALL ACTIVE IN 30 SECONDS..."+CommonFunctions.fetchDateInUTC());
                        }else if(isMusicPlaying(ctx)){
                            Log.i(ForegroundService.LOG_TAG,"MUSIC ACTIVE IN 30 SECONDS..."+CommonFunctions.fetchDateInUTC());
                        }
                    }
                }, 30000);

                Log.i(ForegroundService.LOG_TAG,"SCREEN OFF: "+ CommonFunctions.fetchDateInUTC());
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Toast.makeText(context, "Screen ON", Toast.LENGTH_SHORT).show();
                Preference.getInstance(ctx).put(Preference.Key.IS_REPORT_SENT,false);
                Preference.getInstance(context).put(Preference.Key.SCREEN_ON_TIME,CommonFunctions.fetchDateInUTC());
                Log.i(ForegroundService.LOG_TAG,"SCREEN ON: "+ CommonFunctions.fetchDateInUTC());
            }
        }
    }

    private boolean isCallActive(Context context){
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        boolean isOnCall = false;
        if(manager != null)
            isOnCall = manager.getMode() == AudioManager.MODE_IN_CALL;
        return isOnCall;
    }

    private boolean isMusicPlaying(Context context){
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        boolean isOnMusic = false;
        if(manager != null)
            isOnMusic = manager.isMusicActive();
        return isOnMusic;
    }

    private void startCreatingJSON(Context ctx)
    {
        Preference.getInstance(ctx).put(Preference.Key.IS_REPORT_SENT,true);
        Functions func = new Functions(ctx);
        func.collectedWithReport();
        func.collectedUponChange();

        if(Preference.getInstance(ctx) != null) {
            Boolean checkIfPluggedIn = Preference.getInstance(ctx).isFirstReportEmpty();
            if (checkIfPluggedIn) { //first report after app install
                if (CommonFunctions.fetchTodayDate() != null) {
                    String todayDate = CommonFunctions.fetchTodayDate();
                    Preference.getInstance(ctx).put(Preference.Key.FIRST_REPORT, todayDate);
                    allProbes(ctx);
                    Log.i("FINAL JSON..", "first report");
                }
            } else {
                String getTodayDate = Preference.getInstance(ctx).getFirstReportDate();
                String todayDate = CommonFunctions.fetchTodayDate();
                if (getTodayDate.equals(todayDate)) {
                    selectedProbes(ctx);
                    Log.i("FINAL JSON..", "same date");
                } else {
                    if (todayDate != null) {
                        Preference.getInstance(ctx).put(Preference.Key.FIRST_REPORT, todayDate);
                        allProbes(ctx);
                        Log.i("FINAL JSON..", "Different report");
                    }
                }
            }
        }
    }

    private void allProbes(Context ctx){

        object = new JSONObject();
        try {
            JSONObject jsonObjectDevice = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"DeviceInfo");
            JSONArray jsonObjectInstall = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Install");
            JSONArray jsonObjectUninstall = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"UnInstall");
            JSONArray jsonObjectUsage = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"AppUsage");
            JSONArray jsonObjectSensor = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Sensor");
            JSONArray jsonObjBTState = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Bluetooth_State");
            JSONArray jsonObjBTConn = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Bluetooth_Connection");
            JSONObject jsonObjectWifi = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"WifiConnection");
            JSONArray jsonObjectAirplane = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Airplane");
            JSONArray jsonObjCellTower = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"CellTower");
            JSONArray jsonObjLocation = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Location");
            JSONArray jsonObjBatteryPlug = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"BatteryPlug");
            JSONArray jsonObjAccounts = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Accounts");
            //JSONArray jsonObjAudio = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"AudioFiles");
            //JSONArray jsonObjVideo = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"VideoFiles");
            //JSONArray jsonObjImage = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"ImageFiles");
            //JSONArray jsonObjCall = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"CallLogs");
            //JSONArray jsonObjSms = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"SMS");
            //JSONArray jsonObjContact = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Contacts");
            JSONObject jsonObjBattery = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"Battery");
            JSONObject jsonObjRAM = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"RAM");
            JSONObject jsonObjInternal = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"Internal");
            JSONObject jsonObjExternal = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"External");

            if(CommonFunctions.getDeviceID(ctx) != null){
                object.put("IMEI", CommonFunctions.getDeviceID(ctx));
            }
            object.put("DeviceInfo",jsonObjectDevice);
            object.put("Install",jsonObjectInstall);
            object.put("UnInstall",jsonObjectUninstall);
            object.put("AppUsage",jsonObjectUsage);
            object.put("Sensor_accelerometer",jsonObjectSensor);
            object.put("Bluetooth_State",jsonObjBTState);
            object.put("Bluetooth_Connection",jsonObjBTConn);
            object.put("Wifi_State",jsonObjectWifi);
            object.put("Airplane_Mode",jsonObjectAirplane);
            object.put("CellTower",jsonObjCellTower);
            object.put("Location",jsonObjLocation);
            object.put("Battery_Charging_State",jsonObjBatteryPlug);
            object.put("Accounts",jsonObjAccounts);
            //object.put("Audio_Files",jsonObjAudio);
            //object.put("Video_Files",jsonObjVideo);
            //object.put("Image_Files",jsonObjImage);
            //object.put("Call_Logs",jsonObjCall);
            //object.put("SMS",jsonObjSms);
            //object.put("Contacts",jsonObjContact);
            object.put("Battery",jsonObjBattery);
            object.put("RAM Storage",jsonObjRAM);
            object.put("Internal Storage",jsonObjInternal);
            object.put("External Storage",jsonObjExternal);

            //DatabaseInitializer.deleteProbe(AppDatabase.getAppDatabase(ctx),"FINAL_JSON");

            DatabaseInitializer.addDataWithFlag(AppDatabase.getAppDatabase(ctx), "FINAL_JSON",
                    object.toString(), CommonFunctions.fetchDateInUTC(), "0");

            AwsUploader uploader = new AwsUploader(ctx);
            uploader.submitKinesisRecordTest();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void selectedProbes(Context ctx){

        object = new JSONObject();
        try {
            JSONObject jsonObjectDevice = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"DeviceInfo");
            JSONArray jsonObjectInstall = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Install");
            JSONArray jsonObjectUninstall = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"UnInstall");
            JSONArray jsonObjectUsage = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"AppUsage");
            JSONArray jsonObjectSensor = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Sensor");
            JSONArray jsonObjBTState = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Bluetooth_State");
            JSONArray jsonObjBTConn = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Bluetooth_Connection");
            JSONObject jsonObjectWifi = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"WifiConnection");
            JSONArray jsonObjectAirplane = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Airplane");
            JSONArray jsonObjCellTower = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"CellTower");
            JSONArray jsonObjLocation = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Location");
            JSONArray jsonObjBatteryPlug = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"BatteryPlug");
            JSONObject jsonObjBattery = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"Battery");
            JSONObject jsonObjRAM = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"RAM");
            JSONObject jsonObjInternal = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"Internal");
            JSONObject jsonObjExternal = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"External");

            if(CommonFunctions.getDeviceID(ctx) != null){
                object.put("IMEI", CommonFunctions.getDeviceID(ctx));
            }
            object.put("DeviceInfo",jsonObjectDevice);
            object.put("Install",jsonObjectInstall);
            object.put("UnInstall",jsonObjectUninstall);
            object.put("AppUsage",jsonObjectUsage);
            object.put("Sensor_accelerometer",jsonObjectSensor);
            object.put("Bluetooth_State",jsonObjBTState);
            object.put("Bluetooth_Connection",jsonObjBTConn);
            object.put("Wifi_State",jsonObjectWifi);
            object.put("Airplane_Mode",jsonObjectAirplane);
            object.put("CellTower",jsonObjCellTower);
            object.put("Location",jsonObjLocation);
            object.put("Battery_Charging_State",jsonObjBatteryPlug);
            object.put("Battery",jsonObjBattery);
            object.put("RAM Storage",jsonObjRAM);
            object.put("Internal Storage",jsonObjInternal);
            object.put("External Storage",jsonObjExternal);

            //DatabaseInitializer.deleteProbe(AppDatabase.getAppDatabase(ctx),"FINAL_JSON");

            DatabaseInitializer.addDataWithFlag(AppDatabase.getAppDatabase(ctx), "FINAL_JSON",
                    object.toString(), CommonFunctions.fetchDateInUTC(), "0");

            AwsUploader uploader = new AwsUploader(ctx);
            uploader.submitKinesisRecordTest();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}