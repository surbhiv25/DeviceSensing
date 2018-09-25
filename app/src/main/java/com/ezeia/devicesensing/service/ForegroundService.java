package com.ezeia.devicesensing.service;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.MainActivity;
import com.ezeia.devicesensing.R;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.receivers.AirplaneModeReceiver;
import com.ezeia.devicesensing.receivers.BTStateChangedReceiver;
import com.ezeia.devicesensing.receivers.InstallAppReceiver;
import com.ezeia.devicesensing.receivers.LocationReceiver;
import com.ezeia.devicesensing.receivers.PowerReceiver;
import com.ezeia.devicesensing.receivers.ScreenReceiver;
import com.ezeia.devicesensing.receivers.UninstallAppReceiver;
import com.ezeia.devicesensing.receivers.WifiReceiver;
import com.ezeia.devicesensing.receivers.WifiScanReceiver;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.ezeia.devicesensing.utils.Constants;
import com.ezeia.devicesensing.utils.Functions;
import com.ezeia.devicesensing.utils.Location.FetchLocation;
import com.google.gson.JsonObject;
import com.rvalerio.fgchecker.AppChecker;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

public class ForegroundService extends Service implements SensorEventListener {

    public static final String LOG_TAG = "ForegroundService";
    private BTStateChangedReceiver bluetoothReceiver;
    private PowerReceiver powerReceiver;
    private WifiReceiver wifiReceiver;
    private LocationReceiver locationReceiver;
    private WifiScanReceiver wifiScanReceiver;
    private InstallAppReceiver installAppReceiver;
    private UninstallAppReceiver uninstallAppReceiver;
    private ScreenReceiver screenReceiver;
    private AirplaneModeReceiver airplaneModeReceiver;
    private AppChecker appChecker;
    private Context ctx;
    private SensorManager sensorManager = null;
    private Boolean isIntervalDone = false;
    private static final String PRIMARY_NOTIF_CHANNEL = "default";

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = this;
        Fabric.with(this, new Crashlytics());
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        createNotificationService();
        callReceivers();
        startChecker();

        Functions functions = new Functions(this);
        //functions.primaryKeyData();
        functions.collectedUponUsage();
        //functions.collectedWithActivity();
        functions.collectCellTowerData();

        new FetchLocation(this);
    }

    private void createNotificationService(){

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        RemoteViews notificationView = new RemoteViews(this.getPackageName(), R.layout.notification);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannel();
        }

        Notification notification = new NotificationCompat.Builder(this, PRIMARY_NOTIF_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(Color.parseColor("#00f6d8"))
                .setContent(notificationView)
                .setPriority(Notification.PRIORITY_MIN)
                .setOngoing(true).build();

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
    }

    private void callReceivers(){
        //register BroadcastReceiver

        IntentFilter filterBluetooth = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filterBluetooth.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        bluetoothReceiver = new BTStateChangedReceiver();
        registerReceiver(bluetoothReceiver, filterBluetooth);

        powerReceiver = new PowerReceiver();
        registerReceiver(powerReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        locationReceiver = new LocationReceiver();
        registerReceiver(locationReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        wifiScanReceiver = new WifiScanReceiver();
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        IntentFilter filterScreen = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filterScreen.addAction(Intent.ACTION_SCREEN_OFF);
        screenReceiver = new ScreenReceiver();
        registerReceiver(screenReceiver, filterScreen);

        installAppReceiver = new InstallAppReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_INSTALL_PACKAGE);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        registerReceiver(installAppReceiver,filter);

        uninstallAppReceiver = new UninstallAppReceiver();
        IntentFilter filterUninstall = new IntentFilter();
        filterUninstall.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filterUninstall.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filterUninstall.addDataScheme("package");
        registerReceiver(uninstallAppReceiver,filterUninstall);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        airplaneModeReceiver = new AirplaneModeReceiver();
        registerReceiver(airplaneModeReceiver, intentFilter);

        //not required now -- TEMPERATURE SENSOR
       /* PackageManager PM1= ctx.getPackageManager();
        boolean temp = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            temp = PM1.hasSystemFeature(PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE);
        }
        if(temp)
        {
            Sensor mTemperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            mSensorController.registerListener(ForegroundService.this, mTemperature);

            SensorType sensorType = mSensorTypes.get(1);
            AbstractSensorModel sensorModel = AbstractSensorModel.getSensorModelByType(sensorType,ctx);

            if (sensorModel.isActive()) {
                sensorModel.setActive(false);
            } else {
                sensorModel.setActive(true);
            }
        }
        else
        {
            Log.i(LOG_TAG,"Temperature Sensor Not Available.");
        }*/

        PackageManager PM2= ctx.getPackageManager();
        boolean acc = PM2.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        if(acc)
        {
            Sensor mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(ForegroundService.this, mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);

           /* SensorType sensorType = mSensorTypes.get(0);
            AbstractSensorModel sensorModel = AbstractSensorModel.getSensorModelByType(sensorType,ctx);
            if(sensorModel != null){
                if (sensorModel.isActive()) {
                    sensorModel.setActive(false);
                } else {
                    sensorModel.setActive(true);
                }
            }*/
        }
        else
        {
            Log.i(LOG_TAG,"Accelerometer Sensor Not Available.");
        }

        final Handler handler = new Handler();
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {

                        isIntervalDone = true;
                        String xAcc = "",yAcc ="", zAcc = "", accuracy ="";
                        if(Preference.getInstance(ctx) != null){
                            if(Preference.getInstance(ctx).getAccX() != null){
                                xAcc = Preference.getInstance(ctx).getAccX();
                            }
                            if(Preference.getInstance(ctx).getAccY() != null){
                                yAcc = Preference.getInstance(ctx).getAccY();
                            }
                            if(Preference.getInstance(ctx).getAccZ() != null){
                                zAcc = Preference.getInstance(ctx).getAccZ();
                            }
                            if(Preference.getInstance(ctx).getAccuracy() != null){
                                accuracy = Preference.getInstance(ctx).getAccuracy();
                            }
                            Log.i("TAG","SENSOR GET..."+xAcc+"--"+yAcc+"--"+zAcc+"--"+accuracy);
                            Functions func = new Functions(ctx);
                            func.createJSon(xAcc,yAcc,zAcc,accuracy);
                        }
                    }
                });
            }
        }, 0, 60000);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
//        return ContextCompat.checkSelfPermission(this,
//                Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //moved creating notification from here to onCreate()
        return START_REDELIVER_INTENT;
    }

    private void setupChannel(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel chan1;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            chan1 = new NotificationChannel(
                    PRIMARY_NOTIF_CHANNEL,
                    PRIMARY_NOTIF_CHANNEL,
                    NotificationManager.IMPORTANCE_NONE);

            chan1.setLightColor(Color.TRANSPARENT);
            chan1.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

            if(notificationManager != null)
                notificationManager.createNotificationChannel(chan1);
        }
    }

    private void startChecker() {
        appChecker = new AppChecker();
        appChecker
                .when(getPackageName(), new AppChecker.Listener() {
                    @Override
                    public void onForeground(String packageName) {
                       //  Toast.makeText(getBaseContext(), "Our app is in the foreground.", Toast.LENGTH_SHORT).show();
                    }
                })
                .whenOther(new AppChecker.Listener() {
                    @Override
                    public void onForeground(String packageName) {

                    if(Preference.getInstance(ctx) != null) {
                        Boolean checkIfEmpty = Preference.getInstance(ctx).isPackageNameEmpty();
                        if(checkIfEmpty) {
                            Preference.getInstance(ctx).put(Preference.Key.PACKAGE_NAME, packageName);
                            Preference.getInstance(ctx).put(Preference.Key.START_TIME, getCurrentTime());
                            //Log.i(ForegroundService.LOG_TAG,"FOREGROUND APP NAME: "+ packageName);
                            //Log.i(ForegroundService.LOG_TAG,"FOREGROUND APP START TIME: "+ getCurrentTime());
                           // Toast.makeText(getBaseContext(), "Foreground: " + packageName, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String pluggedInState = Preference.getInstance(ctx).getPackageName();
                            if(!pluggedInState.equals(packageName)) {

                                Preference.getInstance(ctx).put(Preference.Key.CLOSE_TIME, getCurrentTime());
                                //Log.i(ForegroundService.LOG_TAG,"FOREGROUND APP CLOSE TIME: "+ getCurrentTime());

                                String appName = Preference.getInstance(ctx).getPackageName();
                                String startTime = Preference.getInstance(ctx).getStartTime();
                                String closeTime = Preference.getInstance(ctx).getCloseTime();
                                createAndSaveJson(appName,startTime,closeTime);

                                Preference.getInstance(ctx).put(Preference.Key.PACKAGE_NAME, packageName);
                                Preference.getInstance(ctx).put(Preference.Key.START_TIME, getCurrentTime());
                                //Log.i(ForegroundService.LOG_TAG,"FOREGROUND APP NAME: "+ packageName);
                                //Log.i(ForegroundService.LOG_TAG,"FOREGROUND APP START TIME: "+ getCurrentTime());
                                //Toast.makeText(getBaseContext(), "Foreground: " + packageName, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    }
                })
                .timeout(2000)
                .start(this);
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        return mdformat.format(calendar.getTime());
    }

    private void stopChecker() {
        appChecker.stop();
    }

    private void createAndSaveJson(String packgeName, String startTime, String closeTime)
    {
        Functions functions = new Functions(ctx);
        JsonObject objectLoc = functions.fetchLocation();
        Log.i("LOCATION", "Location is..."+objectLoc.toString());

        JsonObject subItems = new JsonObject();
        subItems.addProperty("package_Name",packgeName);
        subItems.addProperty("start_Time",startTime);
        subItems.addProperty("close_Time",closeTime);
        subItems.add("location",objectLoc);
        //Log.i("APP USAGE",subItems.toString());
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(ctx),"AppUsage",subItems.toString(), CommonFunctions.fetchDateInUTC());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(bluetoothReceiver);
        unregisterReceiver(wifiReceiver);
        unregisterReceiver(wifiScanReceiver);
        unregisterReceiver(locationReceiver);
        unregisterReceiver(powerReceiver);
        unregisterReceiver(installAppReceiver);
        unregisterReceiver(uninstallAppReceiver);
        unregisterReceiver(screenReceiver);
        unregisterReceiver(airplaneModeReceiver);
        sensorManager.unregisterListener(this);
        stopChecker();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE)
        {
            float temperature = sensorEvent.values[0];
            Integer tempAccuracy = sensorEvent.accuracy;
            Long tempTimestamp = sensorEvent.timestamp;

        } else if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            Integer accAccuracy = sensorEvent.accuracy;
            Long accTimestamp = sensorEvent.timestamp;

            if(isIntervalDone){
                if(Preference.getInstance(this) != null)
                {
                    isIntervalDone = false;
                    Preference.getInstance(this).put(Preference.Key.ACC_X,String.valueOf(x));
                    Preference.getInstance(this).put(Preference.Key.ACC_Y,String.valueOf(y));
                    Preference.getInstance(this).put(Preference.Key.ACC_Z,String.valueOf(z));
                    Preference.getInstance(this).put(Preference.Key.ACCURACY,String.valueOf(accAccuracy));
                    Log.i("TAG","SENSOR VAL: "+ x +"--"+ y +"--"+ z +"--"+ accAccuracy);
                }
            }
        }
    }

    @Override

    public void onAccuracyChanged(Sensor sensor, int i) { }

}
