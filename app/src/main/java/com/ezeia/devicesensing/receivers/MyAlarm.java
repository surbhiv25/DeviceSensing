package com.ezeia.devicesensing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.service.JsonCreation;
import com.ezeia.devicesensing.utils.CommonFunctions;

public class MyAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ForegroundService.isReportSending = true;
        Preference.getInstance(context).remove(Preference.Key.SCREEN_OFF_TIME);
        Preference.getInstance(context).put(Preference.Key.ALARM_SENDING, CommonFunctions.fetchDateInUTC());
        new JsonCreation(context);
        Log.i(ForegroundService.LOG_TAG,"ALARM CALLED......");
    }
}
