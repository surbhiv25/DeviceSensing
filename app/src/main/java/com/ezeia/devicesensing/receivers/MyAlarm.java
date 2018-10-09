package com.ezeia.devicesensing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.service.JsonCreation;

public class MyAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ForegroundService.isReportSending = true;
        new JsonCreation(context);
        Log.i(ForegroundService.LOG_TAG,"ALARM CALLED......");
    }
}
