package com.ezeia.devicesensing;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.utils.Constants;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import io.fabric.sdk.android.Fabric;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.crashlytics.android.Crashlytics.log;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 100;
    private final int PERMISSION_REQUEST_CODE = 200;
    private TextView txt_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());

        fillStats();
        //btnClick();
        //LogsUtil.readLogs();
    }

    /*private void btnClick(){

        txt_message = findViewById(R.id.txt_message);

        Button btn_pushData = findViewById(R.id.btn_pushData);
        btn_pushData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkInternetConnection(MainActivity.this)){
                    txt_message.setText("");
                    MyTask task = new MyTask(MainActivity.this);
                    task.execute();
                }else{
                    txt_message.setText(R.string.internet_connection);
                }
            }
        });
    }*/

    static class MyTask extends AsyncTask<Void, Void, Boolean> {

        private final WeakReference<MainActivity> activityReference;

        // only retain a weak reference to the activity
        MyTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Void... v) {
            try {
                HttpURLConnection urlc = (HttpURLConnection)(new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(10000);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                log("IOException in connectGoogle())");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);

            // get a reference to the activity if it is still there
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            // modify the activity's UI
            TextView textView = activity.findViewById(R.id.txt_message);
            if(aVoid){
                textView.setText("");
                //ScreenReceiver.startCreatingJSON(activity,textView);
            }else{
                textView.setText(R.string.internet_connection);
            }
        }
    }

    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = null;
        if(manager != null)
            ni = manager.getActiveNetworkInfo();

        return ni != null && ni.getState() == NetworkInfo.State.CONNECTED;
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
                }
                else {
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
        //int resultCamera = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);

        return resultSms == PackageManager.PERMISSION_GRANTED && resultCall == PackageManager.PERMISSION_GRANTED
                && resultContact == PackageManager.PERMISSION_GRANTED && resultPhoneState == PackageManager.PERMISSION_GRANTED
                &&  resultLoc == PackageManager.PERMISSION_GRANTED && resultStorage == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionExtra() {

        ActivityCompat.requestPermissions(this, new String[]{READ_SMS,READ_CALL_LOG,READ_CONTACTS,
                READ_PHONE_STATE,ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

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
                    //boolean cameraAccepted = grantResults[6] == PackageManager.PERMISSION_GRANTED;

                    if (smsAccepted && callLogAccepted && contactsAccepted && phoneAccepted
                            && locationAccepted && storageAccepted ) {
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
                                                                READ_PHONE_STATE,ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
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
