package com.ezeia.devicesensing.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.service.NotificationUtils;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.google.gson.JsonObject;

import io.fabric.sdk.android.Fabric;

import static android.content.Context.LOCATION_SERVICE;

public class LocationReceiver extends BroadcastReceiver {

    private NotificationManager mManager;

    private NotificationUtils mNotificationUtils;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Fabric.with(context, new Crashlytics());

        if(intent.getAction() != null)
            if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                Log.i(ForegroundService.LOG_TAG,"Location is..."+checkIfLocEnabled(context));
                String mode = "";

                if(!checkIfLocEnabled(context) && getLocationMode(context) == 1){
                    Preference.getInstance(context).put(Preference.Key.LOC_LATITUDE,"0.0");
                    Preference.getInstance(context).put(Preference.Key.LOC_LONGITUDE,"0.0");
                    Toast.makeText(context,"Location OFF",Toast.LENGTH_SHORT).show();

                    mNotificationUtils = new NotificationUtils(context);
                    Notification.Builder nb = mNotificationUtils.
                            getAndroidChannelNotification("Heyyy", "Data Gets needs the location to be always ON with High_Accuracy.");

                    mNotificationUtils.getManager().notify(105, nb.build());

                }else if(!checkIfLocEnabled(context) && getLocationMode(context) == 2){
                    Preference.getInstance(context).put(Preference.Key.LOC_LATITUDE,"0.0");
                    Preference.getInstance(context).put(Preference.Key.LOC_LONGITUDE,"0.0");
                    Toast.makeText(context,"Location OFF",Toast.LENGTH_SHORT).show();

                    mNotificationUtils = new NotificationUtils(context);
                    Notification.Builder nb = mNotificationUtils.
                            getAndroidChannelNotification("Heyyy", "Data Gets needs the location to be always ON with High_Accuracy.");

                    mNotificationUtils.getManager().notify(105, nb.build());

                }else if(!checkIfLocEnabled(context)){
                    Preference.getInstance(context).put(Preference.Key.LOC_LATITUDE,"0.0");
                    Preference.getInstance(context).put(Preference.Key.LOC_LONGITUDE,"0.0");
                    Toast.makeText(context,"Location OFF",Toast.LENGTH_SHORT).show();

                    mNotificationUtils = new NotificationUtils(context);
                    Notification.Builder nb = mNotificationUtils.
                            getAndroidChannelNotification("Heyyy", "Data Gets needs the location to be always ON.");

                    mNotificationUtils.getManager().notify(105, nb.build());

                }else if(checkIfLocEnabled(context) && getLocationMode(context) == 3){
                    Toast.makeText(context,"Location ON",Toast.LENGTH_SHORT).show();
                    NotificationManager notificationManager = (NotificationManager) context
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(105);
                }

                if(getLocationMode(context) == 1){
                    mode = "Device Only";
                }else if(getLocationMode(context) == 2){
                    mode = "Battery Saving";
                }else if(getLocationMode(context) == 3){
                    mode = "High Accuracy";
                }

                JsonObject object = new JsonObject();
                object.addProperty("Location_state",checkIfLocEnabled(context));
                object.addProperty("Mode",mode);
                object.addProperty("timestamp",CommonFunctions.fetchDateInUTC());
                DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"Location",object.toString(),CommonFunctions.fetchDateInUTC());
            }
    }

    public static boolean checkIfLocEnabled(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        Boolean isNetworkEnabled = false;
        // getting network status
        //Boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(locationManager != null)
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isNetworkEnabled;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static int getLocationMode(Context context)
    {
        int modeVal = 0;
        try {
            modeVal = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return modeVal;
    }


}
