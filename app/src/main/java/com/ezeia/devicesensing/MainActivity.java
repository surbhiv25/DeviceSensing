package com.ezeia.devicesensing;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.utils.Constants;
import io.fabric.sdk.android.Fabric;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 100;
    private final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());

        fillStats();
    }

    private void fillStats()
    {
        if (hasPermission()){
                if (!checkPermissionExtra()) {
                    requestPermissionExtra();
                } else {
                    Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
                    startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(startIntent);
                        finish();
                    } else {
                        startService(startIntent);
                        finish();
                    }
                }
        }else{
            requestPermission();
        }
    }

    private boolean checkPermissionExtra() {
        int resultSms = ContextCompat.checkSelfPermission(getApplicationContext(), READ_SMS);
        int resultCall = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CALL_LOG);
        int resultContact = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        int resultPhoneState = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        int resultLoc = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int resultStorage = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int resultRead = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);

        return resultSms == PackageManager.PERMISSION_GRANTED && resultCall == PackageManager.PERMISSION_GRANTED
                && resultContact == PackageManager.PERMISSION_GRANTED && resultPhoneState == PackageManager.PERMISSION_GRANTED
                &&  resultLoc == PackageManager.PERMISSION_GRANTED && resultStorage == PackageManager.PERMISSION_GRANTED
                && resultRead == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionExtra() {
        ActivityCompat.requestPermissions(this, new String[]{READ_SMS,READ_CALL_LOG,READ_CONTACTS,
                READ_PHONE_STATE,ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200 :
                if (grantResults.length > 0) {

                    boolean smsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean callLogAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean contactsAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean phoneAccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean locationAccepted = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[5] == PackageManager.PERMISSION_GRANTED;
                    boolean readAccepted = grantResults[6] == PackageManager.PERMISSION_GRANTED;

                    if (smsAccepted && callLogAccepted && contactsAccepted && phoneAccepted
                            && locationAccepted && storageAccepted && readAccepted ) {
                        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        //}
                        Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
                        startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        {
                            startForegroundService(startIntent);
                            finish();
                        }
                        else
                        {
                            startService(startIntent);
                            finish();
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel(
                                        new DialogInterface.OnClickListener() {
                                            @TargetApi(Build.VERSION_CODES.M)
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                requestPermissions(new String[]{READ_SMS,READ_CALL_LOG,READ_CONTACTS,
                                                                READ_PHONE_STATE,ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE,
                                                                READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.allowPermission)
                .setPositiveButton(R.string.okButton, okListener)
                .setNegativeButton(R.string.cancelButton, null)
                .create()
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS:
                fillStats();
                break;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if(appOps != null) mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());
        }
        return mode == AppOpsManager.MODE_ALLOWED;
//        return ContextCompat.checkSelfPermission(this,
//                Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED;
    }
}
