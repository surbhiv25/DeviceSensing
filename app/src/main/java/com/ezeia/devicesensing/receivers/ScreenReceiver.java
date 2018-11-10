package com.ezeia.devicesensing.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.service.JsonCreation;
import com.ezeia.devicesensing.utils.CommonFunctions;

import java.util.Calendar;

import io.fabric.sdk.android.Fabric;

public class ScreenReceiver extends BroadcastReceiver{

    private Context ctx;

    @Override
    public void onReceive(Context context, Intent intent) {
            ctx = context;
        Fabric.with(context, new Crashlytics());
        if(intent.getAction() != null)
        {
            final Handler handler = new Handler();

            switch (intent.getAction()) {
                case Intent.ACTION_SCREEN_OFF:
                    Toast.makeText(context, "Screen OFF", Toast.LENGTH_SHORT).show();
                    ForegroundService.screenOnOffStatus = false;
                    Preference.getInstance(ctx).put(Preference.Key.IS_HANDLER_CALLED, false);
                    Preference.getInstance(context).remove(Preference.Key.ALARM_SENDING);
                    Preference.getInstance(context).put(Preference.Key.SCREEN_OFF_TIME, CommonFunctions.fetchDateInUTC());
                    cancelCurrentAlarm(context);

                    handler.postDelayed(new Runnable() {
                        public void run() {
                            if (!isCallActive(ctx) && !isMusicPlaying(ctx)) {
                                Log.i(ForegroundService.LOG_TAG, "TIME AFTER 30 SECONDS..." + CommonFunctions.fetchDateInUTC());
                                if (Preference.getInstance(ctx) != null) {
                                    if (!Preference.getInstance(ctx).getHandlerCalledStatus() || Preference.getInstance(ctx).getReportSentStatus()) {
                                        ForegroundService.isReportSending = true;
                                        new JsonCreation(ctx);
                                        Log.i(ForegroundService.LOG_TAG, "COMING HERE TO SEND DATA....");
                                    }
                                }
                            } else if (isCallActive(ctx)) {
                                Log.i(ForegroundService.LOG_TAG, "CALL ACTIVE IN 30 SECONDS..." + CommonFunctions.fetchDateInUTC());
                            } else if (isMusicPlaying(ctx)) {
                                Log.i(ForegroundService.LOG_TAG, "MUSIC ACTIVE IN 30 SECONDS..." + CommonFunctions.fetchDateInUTC());
                            }
                        }
                    }, 30000);

                    Log.i(ForegroundService.LOG_TAG, "SCREEN OFF: " + CommonFunctions.fetchDateInUTC());
                    break;
                case Intent.ACTION_SCREEN_ON:

                    Toast.makeText(context, "Screen ON", Toast.LENGTH_SHORT).show();
                    ForegroundService.screenOnOffStatus = true;
                    Preference.getInstance(ctx).put(Preference.Key.IS_HANDLER_CALLED, true);
                    Preference.getInstance(context).put(Preference.Key.SCREEN_ON_TIME, CommonFunctions.fetchDateInUTC());
                    Log.i(ForegroundService.LOG_TAG, "SCREEN ON: " + CommonFunctions.fetchDateInUTC());

                    setAlarmAgain(context);
                    break;
                case Intent.ACTION_USER_PRESENT:
                    Log.i(ForegroundService.LOG_TAG, "SCREEN UNLOCKED: " + CommonFunctions.fetchDateInUTC());
                    break;
            }
        }
    }

    private boolean isCallActive(Context context){
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        boolean isOnCall = false;
        if(manager != null)
            isOnCall = manager.getMode() == AudioManager.MODE_IN_CALL;
        return isOnCall;
    }

    private boolean isMusicPlaying(Context context){
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        boolean isOnMusic = false;
        if(manager != null)
            isOnMusic = manager.isMusicActive();
        return isOnMusic;
    }

    private void cancelCurrentAlarm(Context ctx){
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(ctx.getApplicationContext(), MyAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctx.getApplicationContext(), 1, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if(alarmManager != null){
            alarmManager.cancel(pendingIntent);
        }
    }

    private void setAlarmAgain(Context ctx){
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(ctx.getApplicationContext(), MyAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctx.getApplicationContext(), 1, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 5);
        long afterTwoMinutes = c.getTimeInMillis();
        long interval = 60 * 1000 * 5; // 5 minute

        if(alarmManager != null){
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, afterTwoMinutes, interval,pendingIntent);
            Toast.makeText(ctx, "Alarm is set", Toast.LENGTH_SHORT).show();
        }
    }

}