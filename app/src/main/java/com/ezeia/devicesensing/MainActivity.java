package com.ezeia.devicesensing;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.pref.AuthPreferences;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.service.GmailService;
import com.ezeia.devicesensing.utils.Constants;

import java.util.Set;

import io.fabric.sdk.android.Fabric;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 100;
    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_NOTIFICATION_ACCESS = 300;
    private final int PERMISSION_REQUEST_CODE = 200;

    private final String SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
    private static final int AUTHORIZATION_CODE = 1993;
    private static final int ACCOUNT_CODE = 1601;
    private AccountManager am = null;
    private AuthPreferences authPreferences;
    ProgressDialog dialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());

        fillStats();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void fillStats()
    {
        if (hasPermission()){
            if(!checkNotificationAccess()){
                requestNotifictnAccess();
            }else{

               // googleAccessAuthorise();
                if (!checkRuntimePermission()) {
                    requestRuntimePermission();
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
            }
        }else{
            requestAppUsagePermission();
        }
    }

    private boolean checkNotificationAccess(){
        boolean enabled = false;
        Set<String> enabledListenerPackagesSet = NotificationManagerCompat.getEnabledListenerPackages(MainActivity.this);
        for (String string: enabledListenerPackagesSet)
            if (string.contains(getPackageName())) enabled = true;

        return enabled;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void requestNotifictnAccess(){
        startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),MY_PERMISSIONS_REQUEST_PACKAGE_NOTIFICATION_ACCESS);
    }

    private boolean checkRuntimePermission() {
        int resultSms = ContextCompat.checkSelfPermission(getApplicationContext(), READ_SMS);
        int resultCall = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CALL_LOG);
        int resultContact = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        int resultPhoneState = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        int resultLoc = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int resultStorage = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return resultSms == PackageManager.PERMISSION_GRANTED && resultCall == PackageManager.PERMISSION_GRANTED
                && resultContact == PackageManager.PERMISSION_GRANTED && resultPhoneState == PackageManager.PERMISSION_GRANTED
                &&  resultLoc == PackageManager.PERMISSION_GRANTED && resultStorage == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(this, new String[]{READ_SMS,READ_CALL_LOG,READ_CONTACTS,
                READ_PHONE_STATE,ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200 :
                if (grantResults.length > 0) {

                    boolean smsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean callLogAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean contactsAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean phoneAccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean locationAccepted = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[5] == PackageManager.PERMISSION_GRANTED;

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
                                                                READ_PHONE_STATE,ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE,},
                                                        PERMISSION_REQUEST_CODE);
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS:
                fillStats();
                break;
            case MY_PERMISSIONS_REQUEST_PACKAGE_NOTIFICATION_ACCESS:
                fillStats();
                break;
            case AUTHORIZATION_CODE:
                requestToken();
                break;
            case ACCOUNT_CODE:
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                authPreferences.setUser(accountName);

                // invalidate old tokens which might be cached. we want a fresh
                // one, which is guaranteed to work
                invalidateToken();
                requestToken();
                break;
        }
    }

    private void requestAppUsagePermission() {
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
    }

    private void googleAccessAuthorise(){
        am = AccountManager.get(this);

        authPreferences = new AuthPreferences(this);

        if (authPreferences.getUser() != null) {
            invalidateToken();
            requestToken();
        } else {
            chooseAccount();
        }
    }

    private void chooseAccount() {
        // use https://github.com/frakbot/Android-AccountChooser for
        // compatibility with older devices
        Intent intent = AccountManager.newChooseAccountIntent(null, null,
                new String[] { "com.google" }, false, null, null, null, null);
        startActivityForResult(intent, ACCOUNT_CODE);
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle>
    {
        @Override
        public void run(AccountManagerFuture<Bundle> result)
        {
            if(dialog != null){
                dialog.dismiss();
            }
            try {
                Bundle bundle = result.getResult();
                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if(launch != null)
                {
                    startActivityForResult(launch, AUTHORIZATION_CODE);
                } else {
                    String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    Log.i("TAG","TOKEN IS...."+token);
                    authPreferences.setToken(token);

                    Intent cbIntent =  new Intent();
                    cbIntent.setClass(MainActivity.this, GmailService.class);
                    startService(cbIntent);

                    if (!checkRuntimePermission()) {
                        requestRuntimePermission();
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
                }
            } catch (Exception e){
                Log.e("ERROR", e.getMessage(), e);
                //throw new RuntimeException(e);
                authPreferences.setToken("NA");
            }
        }
    }

    private void requestToken()
    {
        am = AccountManager.get(MainActivity.this);
        Account userAccount = null;
        String user = authPreferences.getUser();
        Account[] accounts = AccountManager.get(getApplicationContext()).getAccountsByType("com.google");
        for (Account account : accounts) {
            if (account.name.equals(user)) {
                userAccount = account;
                break;
            }
        }

        Bundle options = new Bundle();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Getting permission");
        dialog.show();
        am.getAuthToken(
                userAccount,                     // Account retrieved using getAccountsByType()
                "oauth2:" + SCOPE,            // Auth scope
                options,                        // Authenticator-specific options
                MainActivity.this,                           // Your activity
                new OnTokenAcquired(),       // Callback called when a token is successfully acquired
                null);    // Callback called if an error occurs
    }

    /**
     * call this method if your token expired, or you want to request a new
     * token for whatever reason. call requestToken() again afterwards in order
     * to get a new token.
     */
    private void invalidateToken() {
        AccountManager accountManager = AccountManager.get(this);
        accountManager.invalidateAuthToken("com.google",
                authPreferences.getToken());

        authPreferences.setToken(null);
    }

}
