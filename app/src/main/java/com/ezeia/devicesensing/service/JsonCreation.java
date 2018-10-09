package com.ezeia.devicesensing.service;

import android.content.Context;
import android.util.Log;

import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.service.awstask.AwsUploader;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.ezeia.devicesensing.utils.Functions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonCreation {

    private JSONObject object;

    public JsonCreation(Context ctx) {
        startCreatingJSON(ctx);
    }

    private void startCreatingJSON(Context ctx)
    {
        Preference.getInstance(ctx).put(Preference.Key.IS_HANDLER_CALLED,false);
        Preference.getInstance(ctx).put(Preference.Key.IS_REPORT_SENT,false);
        Functions func = new Functions(ctx);
        func.collectedWithReport();
        func.collectedUponChange();
        func.collectedUponUsage();
        func.collectedWithActivity();

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
            JSONObject jsonObjLocationInfo = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"LocationInfo");
            JSONArray jsonObjBatteryPlug = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"BatteryPlug");
            JSONArray jsonObjAccounts = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Accounts");
            //JSONArray jsonObjAudio = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"AudioFiles");
            //JSONArray jsonObjVideo = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"VideoFiles");
            //JSONArray jsonObjImage = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"ImageFiles");
            //JSONArray jsonObjCall = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"CallLogs");
            //JSONArray jsonObjSms = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"SMS");
            //JSONArray jsonObjContact = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Contacts");
            JSONArray jsonObjServices = DatabaseInitializer.fetchSingleArray(AppDatabase.getAppDatabase(ctx),"ActiveServices");
            JSONArray jsonObjUserApp = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"UserInstalledApps");
            JSONArray jsonObjSystemApp = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"SystemApps");
            JSONObject jsonObjBattery = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"Battery");
            JSONObject jsonObjRAM = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"RAM");
            JSONObject jsonObjInternal = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"Internal");
            JSONObject jsonObjExternal = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"External");

            if(CommonFunctions.getDeviceID(ctx) != null){
                object.put("IMEI", CommonFunctions.getDeviceID(ctx)); }
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
            object.put("LocationInfo",jsonObjLocationInfo);
            object.put("Battery_Charging_State",jsonObjBatteryPlug);
            object.put("Accounts",jsonObjAccounts);
            //object.put("Audio_Files",jsonObjAudio);
            //object.put("Video_Files",jsonObjVideo);
            //object.put("Image_Files",jsonObjImage);
            //object.put("Call_Logs",jsonObjCall);
            //object.put("SMS",jsonObjSms);
            //object.put("Contacts",jsonObjContact);
            object.put("Active_Services",jsonObjServices);
            object.put("UserInstalled_Apps",jsonObjUserApp);
            object.put("System_Apps",jsonObjSystemApp);
            object.put("Battery",jsonObjBattery);
            object.put("RAM Storage",jsonObjRAM);
            object.put("Internal Storage",jsonObjInternal);
            object.put("External Storage",jsonObjExternal);

            //DatabaseInitializer.deleteProbe(AppDatabase.getAppDatabase(ctx),"FINAL_JSON");

            if(Preference.getInstance(ctx) != null){
                if(!Preference.getInstance(ctx).getHandlerCalledStatus() || Preference.getInstance(ctx).getReportSentStatus()){
                    DatabaseInitializer.addDataWithFlag(AppDatabase.getAppDatabase(ctx), "FINAL_JSON",
                            object.toString(), CommonFunctions.fetchDateInUTC());

                    AwsUploader uploader = new AwsUploader(ctx);
                    uploader.submitKinesisRecordTest();
                }
            }
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
            JSONObject jsonObjLocationInfo = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"LocationInfo");
            JSONArray jsonObjBatteryPlug = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"BatteryPlug");
            JSONArray jsonObjAccounts = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Accounts");
            JSONArray jsonObjServices = DatabaseInitializer.fetchSingleArray(AppDatabase.getAppDatabase(ctx),"ActiveServices");
            JSONArray jsonObjUserApp = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"UserInstalledApps");
            JSONArray jsonObjSystemApp = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"SystemApps");
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
            object.put("LocationInfo",jsonObjLocationInfo);
            object.put("Battery_Charging_State",jsonObjBatteryPlug);
            object.put("Accounts",jsonObjAccounts);
            object.put("Active_Services",jsonObjServices);
            object.put("UserInstalled_Apps",jsonObjUserApp);
            object.put("System_Apps",jsonObjSystemApp);
            object.put("Battery",jsonObjBattery);
            object.put("RAM Storage",jsonObjRAM);
            object.put("Internal Storage",jsonObjInternal);
            object.put("External Storage",jsonObjExternal);

            //DatabaseInitializer.deleteProbe(AppDatabase.getAppDatabase(ctx),"FINAL_JSON");

            if(Preference.getInstance(ctx) != null){
                if(!Preference.getInstance(ctx).getHandlerCalledStatus() || Preference.getInstance(ctx).getReportSentStatus()){
                    DatabaseInitializer.addDataWithFlag(AppDatabase.getAppDatabase(ctx), "FINAL_JSON",
                            object.toString(), CommonFunctions.fetchDateInUTC());

                    AwsUploader uploader = new AwsUploader(ctx);
                    uploader.submitKinesisRecordTest();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
