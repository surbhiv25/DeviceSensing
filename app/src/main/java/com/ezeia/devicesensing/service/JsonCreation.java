package com.ezeia.devicesensing.service;

import android.content.Context;
import android.util.Log;

import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.entity.DataFetch;
import com.ezeia.devicesensing.SqliteRoom.entity.GmailData;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.service.awstask.AwsUploader;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.ezeia.devicesensing.utils.Functions;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class JsonCreation {

    private JSONObject object;
    private Context ctx;

    public JsonCreation(Context ctx) {
        this.ctx = ctx;
        startCreatingJSON(ctx);
    }

    private void startCreatingJSON(Context ctx)
    {
      /*  if(!ForegroundService.screenOnOffStatus){
            List<GmailData> listData = DatabaseInitializer.fetchGmailData(AppDatabase.getAppDatabase(ctx));
            for(GmailData data: listData){
                createJSon(data.getFrom(),data.getTo(),data.getDate(),data.getCategory(),data.getSnippet(),data.getMsgID(),data.getMailThreadID(),
                        data.getSubject());
            }
            DatabaseInitializer.deleteByLimit(AppDatabase.getAppDatabase(ctx));
        }*/

        Preference.getInstance(ctx).put(Preference.Key.IS_HANDLER_CALLED,false);
        Preference.getInstance(ctx).put(Preference.Key.IS_REPORT_SENT,false);
        Functions func = new Functions(ctx);
        func.collectedWithReport();
        func.collectedUponChange();
        func.collectedUponUsage();
        func.collectedWithActivity();

        if(Preference.getInstance(ctx) != null) {
            Boolean isThisFirstReport = Preference.getInstance(ctx).isFirstReportEmpty();
            if (isThisFirstReport) { //first report after app install
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

    private void createJSon(String fromSender, String toReceiver, String date, String category, String snippet, String msgID,
                            String mailThreadID, String subject){

        JsonObject object = new JsonObject();
        object.addProperty("from",fromSender);
        object.addProperty("to",toReceiver);
        object.addProperty("date",date);
        object.addProperty("emailId",msgID);
        object.addProperty("mailThreadId",mailThreadID);
        object.addProperty("category",category);
        object.addProperty("subject",subject);
        object.addProperty("snippet",snippet);

        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"Gmail Api",object.toString(),CommonFunctions.fetchDateInUTC());
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
            JSONArray jsonObjNotifictn = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Notification_Stats");
            JSONObject jsonObjBattery = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"Battery");
            JSONObject jsonObjRAM = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"RAM");
            JSONObject jsonObjInternal = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"Internal");
            JSONObject jsonObjExternal = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"External");
            String screenOffTime = "",alarmSent = "";

            if(CommonFunctions.getDeviceID(ctx) != null){
                object.put("IMEI", CommonFunctions.getDeviceID(ctx)); }
            object.put("DeviceInfo",jsonObjectDevice);
            object.put("Install",jsonObjectInstall);
            object.put("UnInstall",jsonObjectUninstall);
            object.put("AppUsage",jsonObjectUsage);

          /*  if(!ForegroundService.screenOnOffStatus){
                JSONArray jsonObjectGmailApi = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Gmail Api");
                object.put("Gmail Api",jsonObjectGmailApi);
            }else{
                JSONArray jsonObjectGmailApi = new JSONArray();
                object.put("Gmail Api",jsonObjectGmailApi);
            }*/

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
            object.put("Notification_Stats",jsonObjNotifictn);
            object.put("Battery",jsonObjBattery);
            object.put("RAM Storage",jsonObjRAM);
            object.put("Internal Storage",jsonObjInternal);
            object.put("External Storage",jsonObjExternal);
          /*  if(Preference.getInstance(ctx) != null){
                if(Preference.getInstance(ctx).isScreenOffEmpty()){
                    alarmSent = Preference.getInstance(ctx).getAlarmSentTime();
                    object.put("Alarm Called Time",alarmSent);

                }else {
                    screenOffTime = Preference.getInstance(ctx).getScreenOffTime();
                    object.put("Screen Off Time",screenOffTime);

                }
            }*/

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
            JSONArray jsonObjUserApp = new JSONArray();
            JSONArray jsonObjSystemApp = new JSONArray();
            JSONArray jsonObjNotifictn = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Notification_Stats");
            JSONObject jsonObjBattery = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"Battery");
            JSONObject jsonObjRAM = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"RAM");
            JSONObject jsonObjInternal = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"Internal");
            JSONObject jsonObjExternal = DatabaseInitializer.fetchJsonData(AppDatabase.getAppDatabase(ctx),"External");
            String screenOffTime = "",alarmSent = "";

            if(CommonFunctions.getDeviceID(ctx) != null){
                object.put("IMEI", CommonFunctions.getDeviceID(ctx));
            }
            object.put("DeviceInfo",jsonObjectDevice);
            object.put("Install",jsonObjectInstall);
            object.put("UnInstall",jsonObjectUninstall);
            object.put("AppUsage",jsonObjectUsage);

            /*if(!ForegroundService.screenOnOffStatus){
                JSONArray jsonObjectGmailApi = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"Gmail Api");
                object.put("Gmail Api",jsonObjectGmailApi);
            }else{
                JSONArray jsonObjectGmailApi = new JSONArray();
                object.put("Gmail Api",jsonObjectGmailApi);
            }*/
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
            object.put("Notification_Stats",jsonObjNotifictn);
            object.put("Battery",jsonObjBattery);
            object.put("RAM Storage",jsonObjRAM);
            object.put("Internal Storage",jsonObjInternal);
            object.put("External Storage",jsonObjExternal);

           /* if(Preference.getInstance(ctx) != null){
                if(Preference.getInstance(ctx).isScreenOffEmpty()){
                    alarmSent = Preference.getInstance(ctx).getAlarmSentTime();
                    object.put("Alarm Called Time",alarmSent);
                }else {
                    screenOffTime = Preference.getInstance(ctx).getScreenOffTime();
                    object.put("Screen Off Time",screenOffTime);

                }
            }*/

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
