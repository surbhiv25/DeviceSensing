package com.ezeia.devicesensing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.utils.Constants;

import io.fabric.sdk.android.Fabric;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Fabric.with(context, new Crashlytics());
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent startIntent = new Intent(context, ForegroundService.class);
            startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent);
            }
            else {
                context.startService(startIntent);
            }
        }
    }
}
