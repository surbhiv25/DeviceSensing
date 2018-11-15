package com.ezeia.devicesensing.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.an.deviceinfo.device.model.Battery;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.service.ForegroundService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

public class CommonFunctions {

    //android ID
    @SuppressLint("HardwareIds")
    public static String getAndroidID(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    //device Serial ID- IMEI
    @SuppressLint("HardwareIds")
    public static String getDeviceID(Context ctx) {
        TelephonyManager telephonyMgr = (TelephonyManager) ctx.getSystemService(TELEPHONY_SERVICE);
        String m_deviceId = "";
        if(telephonyMgr != null)
        {
            try {
                m_deviceId = telephonyMgr.getDeviceId();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        return m_deviceId;
    }

    //device model and android version
    public static String getDeviceModelAndVersion() {
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        return model + "^" + version;
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceSerialID() {
        String SerialNo = "";
        if(Build.SERIAL != null)
        {
            SerialNo = Build.SERIAL;
        }
        return SerialNo;
    }

    //service provider
    @SuppressLint("HardwareIds")
    public static LinkedHashMap<String,String> getNetwrkProvider(Context ctx) {
        TelephonyManager manager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName,networkOperator,line1Number,netwrkCountry,simCountryISo,simOperator,
                simOperatorName,simSerialNo,subscriberId,voicemailTag,voicemailNum;
        int callState,netwrkType,phoneType,simState;

        LinkedHashMap<String,String> hmap = new LinkedHashMap<>();
        try {
            if (manager != null) {
                carrierName = manager.getNetworkOperatorName();
                callState=manager.getCallState();
                networkOperator=manager.getNetworkOperator();
                line1Number=manager.getLine1Number();
                netwrkCountry=manager.getNetworkCountryIso();
                netwrkType=manager.getNetworkType();
                phoneType=manager.getPhoneType();
                simCountryISo=manager.getSimCountryIso();
                simOperator=manager.getSimOperator();
                simOperatorName=manager.getSimOperatorName();
                simSerialNo=manager.getSimSerialNumber();
                simState = manager.getSimState();
                subscriberId = manager.getSubscriberId();
                voicemailTag = manager.getVoiceMailAlphaTag();
                voicemailNum = manager.getVoiceMailNumber();

                hmap.put("Carrier Name",carrierName);
                hmap.put("Call State",String.valueOf(callState));
                hmap.put("Operator",networkOperator);
                hmap.put("Line 1 Number",line1Number);
                hmap.put("Country",netwrkCountry);
                hmap.put("Phone Type",String.valueOf(phoneType));
                hmap.put("Network Type",String.valueOf(netwrkType));
                hmap.put("Sim Country",simCountryISo);
                hmap.put("Sim Operator",simOperator);
                hmap.put("Sim Operator Name",simOperatorName);
                hmap.put("Sim Serial Num",simSerialNo);
                hmap.put("Sim State",String.valueOf(simState));
                hmap.put("Subscriber ID",subscriberId);
                hmap.put("Voicemail Alpha Tag",voicemailTag);
                hmap.put("Voicemail Num",voicemailNum);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return hmap;
    }

    //device MAC Address (add permission WIFI_STATE )
    @SuppressLint("HardwareIds")
    public static String getDeviceMACAddress(Context ctx) {
        WifiManager wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo;
        String macAddress = "";
        if(wifiManager != null){
            wInfo = wifiManager.getConnectionInfo();
            if(wInfo != null)
                macAddress = wInfo.getMacAddress();
        }

        return macAddress;
    }

    public static void getBatteryInfo(Context ctx)
    {
        Battery battery=new Battery(ctx);
        String batteryHealth=battery.getBatteryHealth();
        String batteryTech=battery.getBatteryTechnology();
        String batteryChrgingSource=battery.getChargingSource();
        int batteryPercent=battery.getBatteryPercent();
        Float batteryTemp=battery.getBatteryTemperature();
        int batteryVoltage=battery.getBatteryVoltage();
        boolean isBatteryPresent=battery.isBatteryPresent();
        boolean isPhoneCharging=battery.isPhoneCharging();

        JsonObject object = new JsonObject();
        object.addProperty("Health",batteryHealth);
        object.addProperty("Battery Tech",batteryTech);
        object.addProperty("Charging Source",batteryChrgingSource);
        object.addProperty("Battery Level",batteryPercent);
        object.addProperty("Temperature",batteryTemp);
        object.addProperty("Voltage",batteryVoltage);
        object.addProperty("Battery Present",isBatteryPresent);
        object.addProperty("Phone Charging",isPhoneCharging);
        object.addProperty("Timestamp",CommonFunctions.fetchDateInUTC());

        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"Battery",object.toString(),CommonFunctions.fetchDateInUTC());
    }

    public static void getAllSms(Context ctx) {
        Sms objSms;
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = ctx.getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        int totalSMS;
        if(c != null) {
            totalSMS = c.getCount();

            if (c.moveToFirst()) {
                JsonArray jsonArray = new JsonArray();
                for (int i = 0; i < totalSMS; i++) {

                    objSms = new Sms();
                    objSms.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
                    objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                    objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                    objSms.setLocked(c.getString(c.getColumnIndexOrThrow("locked")));
                    objSms.setPerson(c.getString(c.getColumnIndexOrThrow("person")));
                    objSms.setProtocol(c.getString(c.getColumnIndexOrThrow("protocol")));
                    objSms.setReadState(c.getString(c.getColumnIndex("read")));
                    objSms.setReplyPath(c.getString(c.getColumnIndexOrThrow("reply_path_present")));
                    objSms.setServiceCenter(c.getString(c.getColumnIndexOrThrow("service_center")));
                    objSms.setStatus(c.getString(c.getColumnIndexOrThrow("status")));
                    objSms.setSubject(c.getString(c.getColumnIndexOrThrow("subject")));
                    objSms.setId(c.getString(c.getColumnIndexOrThrow("thread_id")));

                    JsonObject person = new JsonObject();
                    person.addProperty("Address", "DemoAddress+123");
                    person.addProperty("Body","Hi this is a demo msg.");
                    //person.addProperty("Address", objSms.getAddress());
                    //person.addProperty("Body", objSms.getMsg());
                    person.addProperty("Date", objSms.getTime());
                    person.addProperty("Locked", objSms.getLocked());
                    person.addProperty("Person", objSms.getPerson());
                    person.addProperty("Protocol", objSms.getProtocol());
                    person.addProperty("Read", objSms.getReadState());
                    person.addProperty("Reply_Path_Present", objSms.getReplyPath());
                    person.addProperty("Service_Center", objSms.getServiceCenter());
                    person.addProperty("Subject", objSms.getSubject());
                    person.addProperty("Status", objSms.getStatus());
                    person.addProperty("Thread_ID", objSms.getId());

                    if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                        objSms.setFolderName("inbox");
                        person.addProperty("Folder_Name", objSms.getFolderName());
                    } else {
                        objSms.setFolderName("sent");
                        person.addProperty("Folder_Name", objSms.getFolderName());
                    }

                    jsonArray.add(person);
                    c.moveToNext();
                }
                //header.add("SMS",jsonArray);
                //Log.i("JSON",header.toString());
                DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx), "SMS", jsonArray.toString(), CommonFunctions.fetchDateInUTC());
            }
            c.close();
        }
    }

    public static long freeRamMemorySize(Context ctx) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
        if(activityManager != null)
            activityManager.getMemoryInfo(mi);

        return mi.availMem;
    }

    public static long totalRamMemorySize(Context ctx) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager)ctx.getSystemService(ACTIVITY_SERVICE);
        if(activityManager != null)
            activityManager.getMemoryInfo(mi);

        return mi.totalMem;
    }

    public static boolean externalMemoryAvailable() {
        if (Environment.isExternalStorageRemovable()) {
            //device support sd card. We need to check sd card availability.
            String state = Environment.getExternalStorageState();
            return state.equals(Environment.MEDIA_MOUNTED) || state.equals(
                    Environment.MEDIA_MOUNTED_READ_ONLY);
        } else {
            //device not support sd card.
            return false;
        }
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    public static long getAvailableExternalMemorySize() {

            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
    }

    public static long getTotalExternalMemorySize() {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
    }

    public static String formatSize(long size) {
        String suffix = null;

        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        Double sizeVal = (double) size;
        if (sizeVal >= 1024) {
            suffix = " KB";
            sizeVal /= 1024;
            if (sizeVal >= 1024) {
                suffix = " MB";
                sizeVal /= 1024;
                if (sizeVal >= 1024) {
                    suffix = " GB";
                    sizeVal /= 1024;
                }
            }
        }

        StringBuilder resultBuffer = new StringBuilder(twoDecimalForm.format(sizeVal));

       /* int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }*/
        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    private static String returnToDecimal(long values){
        DecimalFormat df = new DecimalFormat("#.##");
        String angle = df.format(values);
        return angle;
    }

    public static void getCallDetails(Context ctx) {

        Cursor managedCursor = null;
        int number, name, numberLabel, numberType, type, date, duration, ID;
        try{
            managedCursor = ctx.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        }catch(SecurityException e){
            e.printStackTrace();
        }

        if (managedCursor != null) {
            number = managedCursor.getColumnIndex( CallLog.Calls.NUMBER );
            name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
            numberLabel = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL);
            numberType = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE);
            type = managedCursor.getColumnIndex( CallLog.Calls.TYPE );
            date = managedCursor.getColumnIndex( CallLog.Calls.DATE);
            duration = managedCursor.getColumnIndex( CallLog.Calls.DURATION);
            ID=managedCursor.getColumnIndex( CallLog.Calls._ID);

            JsonArray jsonArray = new JsonArray();
            while ( managedCursor.moveToNext() ) {
                String phName = managedCursor.getString( name );
                String label = managedCursor.getString( numberLabel );
                String numType = managedCursor.getString( numberType );
                String phNumber = managedCursor.getString( number );
                String callType = managedCursor.getString( type );
                String callDate = managedCursor.getString( date );
                Date callDayTime = new Date(Long.valueOf(callDate));
                String callDuration = managedCursor.getString( duration );
                String callID = managedCursor.getString( ID );
                String dir = null;
                int dircode = Integer.parseInt( callType );
                switch( dircode ) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        break;

                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dir = "MISSED";
                        break;
                }
                JsonObject person = new JsonObject();
                person.addProperty("Phone Number","9874561230");
                //person.addProperty("Phone Number",phNumber);
                person.addProperty("Call Type",dir);
                person.addProperty("Call Date",String.valueOf(callDayTime));
                person.addProperty("Call duration in sec",callDuration);
                person.addProperty("Call ID",callID);
                person.addProperty("Contact Name","XYZ");
                //person.addProperty("Contact Name",phName);
                person.addProperty("Contact label",label);
                person.addProperty("Call Num Type",numType);
                jsonArray.add(person);
            }
            //header.add("CallLogs",jsonArray);
            //Log.i("JSON",header.toString());
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"CallLogs",jsonArray.toString(), CommonFunctions.fetchDateInUTC());
            managedCursor.close();
        }
    }

    public static String fetchDateInUTC() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.ENGLISH);
        return format.format(date);
    }

    public static String fetchTodayDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
        Date date = new Date();
        return df.format(date);
    }

    public static String fetchDateGmail() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd",Locale.ENGLISH);
        Date date = new Date();
        return df.format(date);
    }

    public static String fetchDayDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy HH:mm:ss",Locale.ENGLISH);
        Date date = new Date();
        return df.format(date);
    }

    public static Boolean dateCompare(String dateOne, String dateTwo){
        Boolean check = false;
        try{
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss",Locale.ENGLISH);
            Date date1 = formatter.parse(dateOne);
            Date date2 = formatter.parse(dateTwo);

            if (date2.after(date1))
            {
                check = true;
            }
        }catch (ParseException e1){
            e1.printStackTrace();
        }
        return check;
    }

    public static String fetchTomorrowDateGmail() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd",Locale.ENGLISH);
       /* Date date = new Date();
        df.format(date);*/
        return df.format(tomorrow);
    }

    public static void getContactList(Context ctx)
    {
        ContentResolver cr = ctx.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    JsonArray jsonArray = new JsonArray();
                    if(pCur != null) {
                        while (pCur.moveToNext()) {
                            String ID = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone._ID));

                            String data1 = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.DATA1));
                            String data2 = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.DATA2));
                            String data4 = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.DATA4));

                            String isPrimary = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.IS_PRIMARY));
                            String isSuperPrimary = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY));

                            String rawContactID = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID));

                            String lastContacted = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED));

                            JsonObject person = new JsonObject();
                            person.addProperty("ID", ID);
                            //person.addProperty("Name", name);
                            //person.addProperty("Data 1", data1);
                            person.addProperty("Name", "XYZ");
                            person.addProperty("Data 1", "7894561230");
                            person.addProperty("Data 2", data2);
                            //person.addProperty("Data 4", data4);
                            person.addProperty("Data 4", "7894561230");
                            person.addProperty("Is Primary", isPrimary);
                            person.addProperty("Is Super Primary", isSuperPrimary);
                            person.addProperty("Raw Contact ID", rawContactID);
                            person.addProperty("Last Contacted", lastContacted);
                            jsonArray.add(person);
                        }
                        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"Contacts",jsonArray.toString(), CommonFunctions.fetchDateInUTC());
                        pCur.close();
                    }
                }
            }
            cur.close();
        }
    }

    public static void getAccountInfo(Context ctx)
    {
        LinkedHashMap<String,String> linkedHashMap = new LinkedHashMap<>();
        try{
            Account[] accounts = AccountManager.get(ctx).getAccountsByType("com.google");
            if(accounts.length > 0){
                for (Account account : accounts) {
                    linkedHashMap.put(account.name,account.type);
                }
            }

            Account[] accountsGeneral = AccountManager.get(ctx).getAccounts();
            if(accountsGeneral.length > 0){
                for (Account account : accountsGeneral) {
                    linkedHashMap.put(account.name,account.type);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace(); }

       if(linkedHashMap.size() > 0){
           JsonArray jsonArray = new JsonArray();
           for(Map.Entry<String,String> entry : linkedHashMap.entrySet())
           {
               JsonObject person = new JsonObject();
               person.addProperty("name", entry.getKey());
               person.addProperty("type", entry.getValue());
               jsonArray.add(person);
           }
           DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"Accounts",jsonArray.toString(), CommonFunctions.fetchDateInUTC());
       }
    }

    public static void isServiceRunning(Context ctx) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);

        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();

        if(manager != null)
        {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
                String activeService=service.service.getClassName();
                array.add(new JsonPrimitive(activeService));
            }
            //object.add("Services_running", array);
            Log.i(ForegroundService.LOG_TAG,"SERVICES...."+array.toString());
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"ActiveServices",array.toString(), CommonFunctions.fetchDateInUTC());
        }
    }

    //get system and user installed apps
    public static void getUserInstalledApps(Context ctx) {
        ArrayList<String> list = new ArrayList<>();
        final PackageManager packageManager = ctx.getPackageManager();

        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        JsonArray jsonArray = new JsonArray();
        for (ApplicationInfo appInfo : installedApplications)
        {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
            {
                JsonObject person = new JsonObject();

                String appName = appInfo.loadLabel(ctx.getPackageManager()).toString();
                person.addProperty("AppName", appName);
                person.addProperty("PackageName", appInfo.packageName);
                jsonArray.add(person);
            }
        }
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"UserInstalledApps",jsonArray.toString(), CommonFunctions.fetchDateInUTC());
    }

    public static void getSystemInstalledApps(Context ctx) {
        ArrayList<String> list = new ArrayList<>();
        final PackageManager packageManager = ctx.getPackageManager();

        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        JsonArray jsonArray = new JsonArray();
        for (ApplicationInfo appInfo : installedApplications)
        {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
            {
                JsonObject person = new JsonObject();

                String appName = appInfo.loadLabel(ctx.getPackageManager()).toString();
                person.addProperty("AppName", appName);
                person.addProperty("PackageName", appInfo.packageName);
                jsonArray.add(person);
            }
        }
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"SystemApps",jsonArray.toString(), CommonFunctions.fetchDateInUTC());
    }
}
