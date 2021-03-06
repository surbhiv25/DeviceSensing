package com.ezeia.devicesensing;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import com.squareup.leakcanary.LeakCanary;

public class LeakCanaryApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
        setupLeakCanary();
    }

    private void setupLeakCanary() {
        enabledStrictMode();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    private static void enabledStrictMode() {
        if(BuildConfig.DEBUG){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
                        .detectAll() //
                        .penaltyLog() //
                        .penaltyDeath() //
                        .build());
            }
        }
    }
}