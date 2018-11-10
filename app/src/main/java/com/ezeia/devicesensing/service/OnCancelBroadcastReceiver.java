package com.ezeia.devicesensing.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import static com.ezeia.devicesensing.receivers.LocationReceiver.checkIfLocEnabled;
import static com.ezeia.devicesensing.receivers.LocationReceiver.getLocationMode;

public class OnCancelBroadcastReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
       // int notificationId = intent.getExtras().getInt("NotificationId");
       // if(notificationId == 105){
            if(checkIfLocEnabled(context) && getLocationMode(context) == 3){
                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(105);
            }
      //  }
    }
}
