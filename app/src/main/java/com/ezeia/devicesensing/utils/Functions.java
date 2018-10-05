package com.ezeia.devicesensing.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.receivers.LocationReceiver;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.utils.CellTower.CellTowerStateListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static android.content.Context.TELEPHONY_SERVICE;
import static com.ezeia.devicesensing.service.ForegroundService.isReportSending;

public class Functions
{
    private final Context ctx;

    public Functions(Context ctx)
    {
        this.ctx = ctx;
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

        String androidID = CommonFunctions.getAndroidID(ctx);
        String deviceMAC = CommonFunctions.getDeviceMACAddress(ctx);
        LinkedHashMap<String,String> serviceProvider = CommonFunctions.getNetwrkProvider(ctx);

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
        // 3. App Open Close
        // 4. Accelerometer Sensor
        // 5. Bluetooth
        // 6. wifi
        // 7. cell tower connection Broadcast
        // 8. Accounts
        // 9. Audio
        // 10. Video
        // 11. Image
        // 12. Call
        // 13. Sms

        CommonFunctions.getAccountInfo(ctx);
        //getAudioFilesInfo();
        //getVideoFilesInfo(ctx);
        //getImagesFilesInfo();
        //CommonFunctions.getCallDetails(ctx);
        //CommonFunctions.getAllSms(ctx);
        //CommonFunctions.getContactList(ctx);
    }

    public void collectedWithActivity()
    {
        // 1. Time
        // 2. Location (Lat/Long)
        CommonFunctions.isServiceRunning(ctx);
        CommonFunctions.getUserInstalledApps(ctx);
        CommonFunctions.getSystemInstalledApps(ctx);
    }

    public JsonObject fetchLocation(){

        String latitude = "0.0",longitude = "0.0",altitude = "0.0",accuracy = "0.0",
                speed = "0",bearing = "0",elapsedTime = "0",provider = "null";
        Boolean hasAccuracy = false,hasAltitude = false,
                hasSpeed = false,hasBearing = false,isFromMockProvider = false;

        /* GPSTracker gpstracker=new GPSTracker(ctx);
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
        */

        JsonObject object;
        if(LocationReceiver.checkIfLocEnabled(ctx))
        {
            if(Preference.getInstance(ctx) != null) {
                latitude = Preference.getInstance(ctx).getLatitude();
                longitude = Preference.getInstance(ctx).getLongitude();
                altitude = Preference.getInstance(ctx).getAltitude();
                accuracy = Preference.getInstance(ctx).getLocAccuracy();
                speed = Preference.getInstance(ctx).getSpeed();
                bearing = Preference.getInstance(ctx).getBearing();
                hasAccuracy = Preference.getInstance(ctx).getHasAccuracy();
                hasAltitude = Preference.getInstance(ctx).getHasAltitude();
                hasSpeed = Preference.getInstance(ctx).getHasSpeed();
                hasBearing = Preference.getInstance(ctx).getHasBearing();
                isFromMockProvider = Preference.getInstance(ctx).getMockProvider();
                provider = Preference.getInstance(ctx).getProvider();
                elapsedTime = Preference.getInstance(ctx).getElapsedTime();
            }
        }
        object = new JsonObject();
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

    public void createJSon(String x, String y, String z, String acc){

        /*Functions functions = new Functions(ctx);
        JsonObject objectLoc = functions.fetchLocation();
*/
        JsonObject object = new JsonObject();
        object.addProperty("X",x);
        object.addProperty("Y",y);
        object.addProperty("Z",z);
        object.addProperty("Accuracy",acc);
        object.addProperty("timestamp",CommonFunctions.fetchDateInUTC());
        //object.add("location",objectLoc);
        Log.i("SENSOR SAVE INFO", x+"--"+y+"--"+z);
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"Sensor",object.toString(),CommonFunctions.fetchDateInUTC());
    }

    private void createJsonTower(String simType, String signalStrength){

        JsonObject object = new JsonObject();
        object.addProperty("sim_type",simType);
        object.addProperty("signal_strength",signalStrength);
        object.addProperty("timestamp", CommonFunctions.fetchDateInUTC());

        Log.i(ForegroundService.LOG_TAG, "SIM STRENGTH...."+object.toString());

        /*Functions functions = new Functions(ctx);
        JsonObject objectLoc = functions.fetchLocation();
        object.add("location",objectLoc);
*/
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"CellTower",object.toString(),CommonFunctions.fetchDateInUTC());
    }

    public void collectCellTowerData(){

        createExactTimer();
    }

    private void createExactTimer(){
        ScheduledExecutorService scheduleTaskExecutor= Executors.newScheduledThreadPool(5);

        // This schedule a task to run every 1 minutes:
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if(ForegroundService.screenOnOffStatus && !ForegroundService.isReportSending){
                    if(Preference.getInstance(ctx) != null){
                        String strength = "";
                        String simType = Preference.getInstance(ctx).getCellTower();
                        if(Preference.getInstance(ctx).getCellTowerStrength() != null){
                            strength = Preference.getInstance(ctx).getCellTowerStrength();
                        }
                        Log.i(ForegroundService.LOG_TAG,"Tower Strength..."+strength);
                            createJsonTower(simType,strength);
                        }
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    public void collectedWithReport()
    {
        // 1. Memory usage
        // 2. battery

        Functions functions = new Functions(ctx);
        JsonObject objectLoc = functions.fetchLocation();
        Log.i(ForegroundService.LOG_TAG, "Location is..."+objectLoc.toString());
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"LocationInfo",objectLoc.toString(), CommonFunctions.fetchDateInUTC());

        long totalRamValue = CommonFunctions.totalRamMemorySize(ctx);
        long freeRamValue = CommonFunctions.freeRamMemorySize(ctx);
        long usedRamValue = totalRamValue - freeRamValue;

        JsonObject object = new JsonObject();
        object.addProperty("Total",CommonFunctions.formatSize(totalRamValue));
        object.addProperty("Free",CommonFunctions.formatSize(freeRamValue));
        object.addProperty("Used",CommonFunctions.formatSize(usedRamValue));
        object.addProperty("Timestamp",CommonFunctions.fetchDateInUTC());
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"RAM",object.toString(),CommonFunctions.fetchDateInUTC());

        long totalInternalValue = CommonFunctions.getTotalInternalMemorySize();
        long freeInternalValue = CommonFunctions.getAvailableInternalMemorySize();
        long usedInternalValue = totalInternalValue - freeInternalValue;

        JsonObject objectInternal = new JsonObject();
        objectInternal.addProperty("Total",CommonFunctions.formatSize(totalInternalValue));
        objectInternal.addProperty("Free",CommonFunctions.formatSize(freeInternalValue));
        objectInternal.addProperty("Used",CommonFunctions.formatSize(usedInternalValue));
        objectInternal.addProperty("Timestamp",CommonFunctions.fetchDateInUTC());
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"Internal",objectInternal.toString(),CommonFunctions.fetchDateInUTC());

        if(CommonFunctions.externalMemoryAvailable()){
            long totalExternalValue = CommonFunctions.getTotalExternalMemorySize();
            long freeExternalValue = CommonFunctions.getAvailableExternalMemorySize();
            long usedExternalValue = totalExternalValue - freeExternalValue;

            JsonObject objectExternal = new JsonObject();
            objectExternal.addProperty("Total",CommonFunctions.formatSize(totalExternalValue));
            objectExternal.addProperty("Free",CommonFunctions.formatSize(freeExternalValue));
            objectExternal.addProperty("Used",CommonFunctions.formatSize(usedExternalValue));
            objectExternal.addProperty("Timestamp",CommonFunctions.fetchDateInUTC());
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"External",objectExternal.toString(),CommonFunctions.fetchDateInUTC());
        }else{
            JsonObject objectExternal = new JsonObject();
            objectExternal.addProperty("Total","");
            objectExternal.addProperty("Free","");
            objectExternal.addProperty("Used","");
            objectExternal.addProperty("Timestamp",CommonFunctions.fetchDateInUTC());
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"External",objectExternal.toString(),CommonFunctions.fetchDateInUTC());
        }

        CommonFunctions.getBatteryInfo(ctx);
        //Log.i(TAG,"Get CPU USAGE STATS: "+readUsage());
    }

    private void getAudioFilesInfo()
    {
        //String selection = MediaStore.Audio.Media.DATA+ "=?";
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

        if(cursor != null)
        {
            JsonArray jsonArray = new JsonArray();
            while(cursor.moveToNext()){
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

    private void getVideoFilesInfo(Context ctx)
    {
        //String selection = MediaStore.Video.Media.DATA+ "=?";
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

        if(cursor != null)
        {
            JsonArray jsonArray = new JsonArray();
            while(cursor.moveToNext()){

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
    }

    private void getImagesFilesInfo()
    {
        //String selection = MediaStore.Images.Media.DATA+ "=?";

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

        if(cursor != null)
        {
            JsonArray jsonArray = new JsonArray();
            while(cursor.moveToNext()){

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
