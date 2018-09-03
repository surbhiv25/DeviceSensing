package com.ezeia.devicesensing;

import android.app.Application;
import android.os.StrictMode;

import com.squareup.leakcanary.LeakCanary;

public class ExampleApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        setupLeakCanary();
    }

    protected void setupLeakCanary() {
        enabledStrictMode();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    private static void enabledStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
                .detectAll() //
                .penaltyLog() //
                .penaltyDeath() //
                .build());
    }
}