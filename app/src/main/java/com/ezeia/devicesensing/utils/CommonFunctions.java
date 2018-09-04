package com.ezeia.devicesensing.utils;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.StatFs;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.an.deviceinfo.device.model.Battery;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.BATTERY_SERVICE;
import static android.content.Context.CAMERA_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

public class CommonFunctions {

    /** Address of the network devices stat */
    private static final String NETDEV_PATH  = "/proc/net/dev";

    /** Address of memory information file */
    private static final String MEMINFO_PATH = "/proc/meminfo";

    //android ID
    public static String getAndroidID(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String WifiMACAddress(Context ctx)
    {
        WifiManager manager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        return info.getMacAddress();
    }

    public static String getBluetoothMacAddress() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // if device does not support Bluetooth
        if(mBluetoothAdapter==null){
            return "Device does not support bluetooth";
        }

        return mBluetoothAdapter.getAddress();
    }

    //device Serial ID- IMEI
    public static String getDeviceID(Context ctx) {
        TelephonyManager TelephonyMgr = (TelephonyManager) ctx.getSystemService(TELEPHONY_SERVICE);
        String m_deviceId = "";
        try {
            m_deviceId = TelephonyMgr.getDeviceId();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return m_deviceId;
    }

    public static String getFirmwareVersion(Context ctx) {
        TelephonyManager TelephonyMgr = (TelephonyManager) ctx.getSystemService(TELEPHONY_SERVICE);
        String deviceSoftwareVersion = "";
        try {
            deviceSoftwareVersion=TelephonyMgr.getDeviceSoftwareVersion();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return deviceSoftwareVersion;
    }

    //device model and android version
    public static String getDeviceModelAndVersion() {
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        return model + "^" + version;
    }

    public static String getDeviceSerialID() {
        String SerialNo = "";
        if(Build.SERIAL != null)
        {
            SerialNo = Build.SERIAL;
        }
        return SerialNo;
    }

    //service provider
    public static LinkedHashMap<String,String> getNetwrkProvider(Context ctx) {
        TelephonyManager manager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName,networkOperator,line1Number,netwrkCountry,simCountryISo,simOperator,
                simOperatorName,simSerialNo,subscriberId,voicemailTag,voicemailNum;
        int callState,netwrkType,phoneType,simState;
        StringBuilder buffer = new StringBuilder();
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
                hmap.put("Sim Serial No.",simSerialNo);
                hmap.put("Sim State",String.valueOf(simState));
                hmap.put("Subscriber ID",subscriberId);
                hmap.put("Voicemail Alpha Tag",voicemailTag);
                hmap.put("Voicemail Num",voicemailNum);


                buffer.append("OPERATOR NAME: ").append(carrierName)
                        .append("\n").append("CALL STATE: ").append(callState)
                        .append("\n").append("OPERATOR: ").append(networkOperator)
                        .append("\n").append("LINE 1 NUMBER: ").append(line1Number)
                        .append("\n").append("COUNTRY: ").append(netwrkCountry)
                        .append("\n").append("PHONE TYPE: ").append(phoneType)
                        .append("\n").append("NETWORK TYPE: ").append(netwrkType)
                        .append("\n").append("SIM COUNTRY: ").append(simCountryISo)
                        .append("\n").append("SIM OPERATOR: ").append(simOperator)
                        .append("\n").append("SIM OPERATOR NAME: ").append(simOperatorName)
                        .append("\n").append("SIM SERIAL NO: ").append(simSerialNo)
                        .append("\n").append("SIM STATE: ").append(simState)
                        .append("\n").append("SUBSCRIBER ID: ").append(subscriberId)
                        .append("\n").append("VOICEMAIL ALPHA TAG: ").append(voicemailTag)
                        .append("\n").append("VOICEMAIL NUMBER: ").append(voicemailNum);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return hmap;
    }



    //device MAC Address (add permission WIFI_STATE )
    public static String getDeviceMACAddress(Context ctx) {
        WifiManager wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }

    //get system and user installed apps
    public static ArrayList<String> getInstalledApps(Context ctx, Boolean isSystemApp) {
        ArrayList<String> list = new ArrayList<>();
        final PackageManager packageManager = ctx.getPackageManager();

        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo appInfo : installedApplications)
        {
            if(isSystemApp)
            {
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                {
                    String appName = appInfo.loadLabel(ctx.getPackageManager()).toString();
                    list.add(appName+"--"+
                             appInfo.backupAgentName+"--"+
                            appInfo.className+"--"+
                            appInfo.compatibleWidthLimitDp+"--"+
                            appInfo.dataDir+"--"+
                            appInfo.descriptionRes+"--"+
                            appInfo.enabled+"--"+
                            appInfo.flags+"--"+
                            appInfo.icon+"--"+
                            appInfo.labelRes+"--"+
                            appInfo.largestWidthLimitDp+"--"+
                            appInfo.logo+"--"+
                            appInfo.nativeLibraryDir+"--"+
                            appInfo.packageName+"--"+
                            appInfo.processName+"--"+
                            appInfo.publicSourceDir+"--"+
                            appInfo.requiresSmallestWidthDp+"--"+
                            Arrays.toString(appInfo.sharedLibraryFiles) +"--"+
                            appInfo.sourceDir+"--"+
                            appInfo.targetSdkVersion+"--"+
                            appInfo.taskAffinity+"--"+
                            appInfo.theme+"--"+
                            appInfo.uiOptions+"--"+
                            appInfo.uid);
                    // IS A SYSTEM APP
                }
            }
            else {
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                {
                    String appName = appInfo.loadLabel(ctx.getPackageManager()).toString();
                    list.add(appName+"--"+
                            appInfo.backupAgentName+"--"+
                            appInfo.className+"--"+
                            appInfo.compatibleWidthLimitDp+"--"+
                            appInfo.dataDir+"--"+
                            appInfo.descriptionRes+"--"+
                            appInfo.enabled+"--"+
                            appInfo.flags+"--"+
                            appInfo.icon+"--"+
                            appInfo.labelRes+"--"+
                            appInfo.largestWidthLimitDp+"--"+
                            appInfo.logo+"--"+
                            appInfo.nativeLibraryDir+"--"+
                            appInfo.packageName+"--"+
                            appInfo.processName+"--"+
                            appInfo.publicSourceDir+"--"+
                            appInfo.requiresSmallestWidthDp+"--"+
                            Arrays.toString(appInfo.sharedLibraryFiles) +"--"+
                            appInfo.sourceDir+"--"+
                            appInfo.targetSdkVersion+"--"+
                            appInfo.taskAffinity+"--"+
                            appInfo.theme+"--"+
                            appInfo.uiOptions+"--"+
                            appInfo.uid);
                }
            }
        }
        return list;
    }

    //get running and background tasks
    public static List<ActivityManager.RunningAppProcessInfo> getRunningTasks(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
        return activityManager.getRunningAppProcesses();
    }

    //get running and background tasks
    public static List<ActivityManager.RunningTaskInfo> getRunningApplications(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);

        final List<ActivityManager.RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (int i = 0; i < recentTasks.size(); i++)
        {
            ComponentName componentInfo = recentTasks.get(0).topActivity;
            componentInfo.getPackageName();

            Log.d("Executed app", "Application executed : " +recentTasks.get(i).baseActivity.toShortString()+ "\t\t ID: "+recentTasks.get(i).id+"");
        }
        return recentTasks;
    }

    public static void getProcessInfo(Context ctx)
    {
        ActivityManager am = (ActivityManager)ctx.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

        //processes names
        ArrayList<ActivityManager.RunningAppProcessInfo> runningProcesses = new ArrayList<>(am.getRunningAppProcesses());
        final int numProcesses = runningProcesses.size();
        int[] runningProcessIds = new int[numProcesses];
        for (int i=0; i<numProcesses; i++ ) {
            runningProcessIds[i] = runningProcesses.get(i).pid;
        }

        //process memory data
        List<Debug.MemoryInfo> memoryInfos = Arrays.asList(am.getProcessMemoryInfo(runningProcessIds));

        //process error info
        List<ActivityManager.ProcessErrorStateInfo> errorInfos = am.getProcessesInErrorState();

    }

    public static StringBuffer getBatteryInfo(Context ctx)
    {
        StringBuffer buffer = new StringBuffer();
        Battery battery=new Battery(ctx);
        String batteryHealth=battery.getBatteryHealth();
        String batteryTech=battery.getBatteryTechnology();
        String batteryChrgingSource=battery.getChargingSource();
        int batteryPercent=battery.getBatteryPercent();
        Float batteryTemp=battery.getBatteryTemperature();
        int batteryVoltage=battery.getBatteryVoltage();
        boolean isBatteryPresent=battery.isBatteryPresent();
        boolean isPhoneCharging=battery.isPhoneCharging();

        buffer.append("HEALTH: ").append(batteryHealth)
                .append("\n").append("BATTERY TECH: ").append(batteryTech)
                .append("\n").append("CHARGING SOURCE: ").append(batteryChrgingSource)
                .append("\n").append("BATTERY LEVEL: ").append(batteryPercent)
                .append("\n").append("TEMPERATURE: ").append(batteryTemp)
                .append("\n").append("VOLTAGE: ").append(batteryVoltage)
                .append("\n").append("BATTERY PRESENT: ").append(isBatteryPresent)
                .append("\n").append("PHONE CHARGING: ").append(isPhoneCharging);

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
        return buffer;
    }

    public static int getBatteryLevel(Context ctx) {
        int batLevel;
        if (Build.VERSION.SDK_INT > 21) {
            BatteryManager bm = (BatteryManager) ctx.getSystemService(BATTERY_SERVICE);
            batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            String iconSmall= BatteryManager.EXTRA_ICON_SMALL;
            /*BatteryManager.EXTRA_SCALE;
            BatteryManager.CH*/
        } else {
            batLevel = getBatteryPercentage(ctx);
        }
        return batLevel;
    }

    private static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }

    public static ArrayList<String> printAllSensors(Context context) {
        ArrayList<String> list = new ArrayList<>();
        SensorManager sensorMngr = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sensorMngr.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            // debug or print the follwoing
            sensor.getName();
            list.add(sensor.getName());
        }
        return list;
    }

    public static void checkTemperatureSensor(Context ctx)
    {
        SensorManager mgr = (SensorManager) ctx.getSystemService(SENSOR_SERVICE);
        Sensor AmbientTemperatureSensor = mgr.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        /*if(AmbientTemperatureSensor != null){
            textAMBIENT_TEMPERATURE_available.setText("Sensor.TYPE_AMBIENT_TEMPERATURE Available");
            mySensorManager.registerListener(
                    AmbientTemperatureSensorListener,
                    AmbientTemperatureSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            textAMBIENT_TEMPERATURE_available.setText("Sensor.TYPE_AMBIENT_TEMPERATURE NOT Available");
        }*/
    }

    public static boolean getMicrophoneAvailable(Context context) {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(new File(context.getCacheDir(), "MediaUtil#micAvailTestFile").getAbsolutePath());
        boolean available = true;
        try {
            recorder.prepare();
            recorder.start();

        }
        catch (Exception exception) {
            available = false;
        }
        recorder.release();
        return available;
    }

    public static List<Sms> getAllSms(Context ctx) {
        List<Sms> lstSms = new ArrayList<>();
        Sms objSms;
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = ctx.getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            JsonObject header = new JsonObject();
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
                person.addProperty("Address",objSms.getAddress());
                //person.addProperty("Body",objSms.getMsg());
                person.addProperty("Body","Hi this is a demo msg.");
                person.addProperty("Date",objSms.getTime());
                person.addProperty("Locked",objSms.getLocked());
                person.addProperty("Person",objSms.getPerson());
                person.addProperty("Protocol",objSms.getProtocol());
                person.addProperty("Read",objSms.getReadState());
                person.addProperty("Reply_Path_Present",objSms.getReplyPath());
                person.addProperty("Service_Center",objSms.getServiceCenter());
                person.addProperty("Subject",objSms.getSubject());
                person.addProperty("Status",objSms.getStatus());
                person.addProperty("Thread_ID",objSms.getId());

                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.setFolderName("inbox");
                    person.addProperty("Folder_Name",objSms.getFolderName());
                } else {
                    objSms.setFolderName("sent");
                    person.addProperty("Folder_Name",objSms.getFolderName());
                }

                jsonArray.add(person);

                lstSms.add(objSms);
                c.moveToNext();
            }
            //header.add("SMS",jsonArray);
            //Log.i("JSON",header.toString());
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"SMS",jsonArray.toString(), CommonFunctions.fetchDateInUTC());
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }
        c.close();

        return lstSms;
    }

    public static void fetchCameraDetails(Context ctx)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        CameraManager manager = (CameraManager)ctx.getSystemService(CAMERA_SERVICE);

            try {
                for (String cameraId : manager.getCameraIdList()) {
                    CameraCharacteristics chars = manager.getCameraCharacteristics(cameraId);
                    Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
                    if(facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT)
                    {
                        Log.d("KUCH BHI","Front Cam available");
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static long freeRamMemorySize(Context ctx) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);

        return mi.availMem / 1048576L;
    }

    public static long totalRamMemorySize(Context ctx) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager)ctx.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return mi.totalMem / 1048576L;
    }

    private static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
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
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return 0;
        }
    }

    public static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return 0;
        }
    }

    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = " KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = " MB";
                size /= 1024;
            }
        }
        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }
        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    private String returnToDecimalPlaces(long values){
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(values);
    }


    public static StringBuffer getCallDetails(Context ctx) {

        StringBuffer sb = new StringBuffer();
        LinkedHashMap<String,String> hashMap = new LinkedHashMap<>();
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        Cursor managedCursor = ctx.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = 0;
        if (managedCursor != null) {
            number = managedCursor.getColumnIndex( CallLog.Calls.NUMBER );
        }
        int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int numberLabel = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL);
        int numberType = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE);
        int type = managedCursor.getColumnIndex( CallLog.Calls.TYPE );
        int date = managedCursor.getColumnIndex( CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex( CallLog.Calls.DURATION);
        int ID=managedCursor.getColumnIndex( CallLog.Calls._ID);

        sb.append( "Call Details :");

        JsonObject header = new JsonObject();
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
            sb.append("\nPhone Number:--- ").append(phNumber)
                    .append(" \nCall Type:--- ").append(dir)
                    .append(" \nCall Date:--- ").append(callDayTime)
                    .append(" \nCall duration in sec :--- ").append(callDuration)
                    .append("\n Call ID :--- ").append(callID)
                    .append("\n Contact Name :--- ").append(phName)
                    .append("\n Contact label :--- ").append(label)
                    .append("\n Call Num Type :--- ").append(numType);
            sb.append("\n----------------------------------");

            JsonObject person = new JsonObject();
            person.addProperty("Phone Number","9874563211");
            person.addProperty("Call Type",dir);
            person.addProperty("Call Date",String.valueOf(callDayTime));
            person.addProperty("Call duration in sec",callDuration);
            person.addProperty("Call ID",callID);
            person.addProperty("Contact Name","XYZ");
            person.addProperty("Contact label",label);
            person.addProperty("Call Num Type",numType);
            jsonArray.add(person);

        }
        //header.add("CallLogs",jsonArray);
        //Log.i("JSON",header.toString());
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"CallLogs",jsonArray.toString(), CommonFunctions.fetchDateInUTC());
        managedCursor.close();
        return sb;
    }

    public static String fetchDateInUTC() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.ENGLISH);
        return format.format(date);
    }

    public static String addTime(String fetchedDate) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS",Locale.ENGLISH);
        Date date = df.parse(fetchedDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, 30);
        date = cal.getTime();

        return df.format(date);
    }

    public static StringBuffer getContactList(Context ctx)
    {
        StringBuffer buffer=new StringBuffer();

        ContentResolver cr = ctx.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
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

                        buffer.append("ID: ").append(ID).append("--").append("NAME: ").append(name).append("--").append("DATA1: ").append(data1).append("--").
                                append("DATA2: ").append(data2).append("--").append("DATA4: ").append(data4).append("--")
                                .append("ISPRIMARY: ").append(isPrimary).append("--").append("ISSUPERPRIMARY: ").
                                append(isSuperPrimary).append("--").append("RAW CONTACT ID: ").append(rawContactID).append("--").
                                append("LAST CONTACTED: ").append(lastContacted).append("\n\n");

                        JsonObject person = new JsonObject();
                        person.addProperty("ID",ID);
                        //person.addProperty("Name",name);
                        //person.addProperty("Data 1",data1);
                        person.addProperty("Name","XYZ");
                        person.addProperty("Data 1","1234567890");
                        person.addProperty("Data 2",data2);
                        person.addProperty("Data 4",data4);
                        person.addProperty("Is Primary",isPrimary);
                        person.addProperty("Is Super Primary",isSuperPrimary);
                        person.addProperty("Raw Contact ID",rawContactID);
                        person.addProperty("Last Contacted",lastContacted);
                        jsonArray.add(person);
                    }

                    DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"Contacts",jsonArray.toString(), CommonFunctions.fetchDateInUTC());
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
        return buffer;
    }

    public static LinkedHashMap<String,String> getAccountInfo(Context ctx)
    {
        String possibleEmail="";
        int counter=1;
        LinkedHashMap<String,String> linkedHashMap = new LinkedHashMap<>();

        try{
            possibleEmail += "";
            Account[] accounts = AccountManager.get(ctx).getAccountsByType("com.google");

            for (Account account : accounts) {

                possibleEmail += counter+" --> "+account.name+" : "+account.type+" \n";
                linkedHashMap.put(account.name,account.type);
                //Log.i("JSON",account.name+"--"+account.type);
                //possibleEmail += counter++;
            }
        }
        catch(Exception e)
        {
            Log.i("Exception", "Exception:"+e) ;
        }

        try{
            possibleEmail += "\n\n";
            Account[] accounts = AccountManager.get(ctx).getAccounts();
            for (Account account : accounts) {

                possibleEmail += " --> "+account.name+" : "+account.type+" \n";
                linkedHashMap.put(account.name,account.type);
                //Log.i("JSON",account.name+"--"+account.type);
                //possibleEmail += " n";
            }
        }
        catch(Exception e)
        {
            Log.i("Exception", "Exception:"+e) ;
        }
        return linkedHashMap;
    }

    public static StringBuffer isServiceRunning(Context ctx) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
        StringBuffer buffer=new StringBuffer();
        int count=0;
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            count++;
            Long data=service.activeSince;
            /*service.clientLabel;
            service.clientCount;*/
            //int crash = service.crashCount;
           /* service.flags;
            service.foreground;
            service.lastActivityTime;
            service.pid;
            service.process;
            service.restarting;
            service.service;
            service.uid*/

            String activeService=service.service.getClassName();
            buffer.append(count).append(". ").append(activeService);

            array.add(new JsonPrimitive(activeService));
        }
        object.add("Services_running", array);
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"ActiveServices",object.toString(), CommonFunctions.fetchDateInUTC());
        return buffer;
    }

    public static StringBuffer getCellTowerInfo(Context ctx)
    {
        StringBuffer bufferTower = new StringBuffer();
        TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = null;
        try
        {
            if (telephonyManager != null) {
                cellLocation = (GsmCellLocation)telephonyManager.getCellLocation();
            }

            int cid = cellLocation.getCid();
            int lac = cellLocation.getLac();
            int psc = cellLocation.getPsc();

            bufferTower.append("CID: ").append(cid).append("\nLAC: ").append(lac).append("\nPSC: ").append(psc);
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }

        return bufferTower;
    }

    public static void checkForegroundApp(Context ctx)
    {
       /* ActivityManager am = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
        // The first in the list of RunningTasks is always the foreground task.
        ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
        String foregroundTaskPackageName = foregroundTaskInfo .topActivity.getPackageName();
        PackageManager pm = ctx.getPackageManager();
        PackageInfo foregroundAppPackageInfo = null;
        try {
            foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString();
        String packageName = foregroundAppPackageInfo.packageName;*/

        String packageName = isAppIsInBackground(ctx);

        Functions func = new Functions(ctx);
        func.fetchUSageStats(ctx,packageName);
    }

    private static String isAppIsInBackground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String packageNAme = "";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                  /*  for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }*/
                  packageNAme = processInfo.processName;
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
           /* if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }*/
           packageNAme = componentInfo.getPackageName();
        }

        return packageNAme;
    }
}
