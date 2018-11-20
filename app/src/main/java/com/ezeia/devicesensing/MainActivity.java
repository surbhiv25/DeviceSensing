package com.ezeia.devicesensing;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.pref.AuthPreferences;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.receivers.LocationReceiver;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.service.GmailService;
import com.ezeia.devicesensing.utils.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.fabric.sdk.android.Fabric;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 100;
    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_NOTIFICATION_ACCESS = 300;
    private final int PERMISSION_REQUEST_CODE = 200;

    private final String SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
    private static final int AUTHORIZATION_CODE = 1993;
    private static final int ACCOUNT_CODE = 1601;
    private AccountManager am = null;
    private AuthPreferences authPreferences;
    ProgressDialog dialog;

    private GoogleApiClient mGoogleApiClient = null;

    private Location mLocation;
    private Context ctx = null;
    private final static int REQUEST_CHECK_SETTINGS_GPS=0x1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());
        ctx = MainActivity.this;
        fillStats();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void fillStats()
    {
        setUpGClient();

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
        //commented because not yet getting included in reports -- 18 nov,2018
        /*int resultSms = ContextCompat.checkSelfPermission(getApplicationContext(), READ_SMS);
        int resultCall = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CALL_LOG);*/

        //add these lines when uncommented
        //resultSms == PackageManager.PERMISSION_GRANTED && resultCall == PackageManager.PERMISSION_GRANTED &&

        int resultContact = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        int resultPhoneState = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        int resultLoc = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int resultStorage = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return  resultContact == PackageManager.PERMISSION_GRANTED && resultPhoneState == PackageManager.PERMISSION_GRANTED
                &&  resultLoc == PackageManager.PERMISSION_GRANTED && resultStorage == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRuntimePermission() {
        //add these lines when uncommented
        //{READ_SMS,READ_CALL_LOG,

        ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS,
                READ_PHONE_STATE,ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200 :
                if (grantResults.length > 0) {

                    //add these lines when uncommented and change array size
                    /*boolean smsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean callLogAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;*/
                    boolean contactsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean phoneAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean locationAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    //add these lines when uncommented
                    //smsAccepted && callLogAccepted &&
                    if (contactsAccepted && phoneAccepted && locationAccepted && storageAccepted ) {
                        if(LocationReceiver.checkIfLocEnabled(MainActivity.this)){
                            Intent cbIntent =  new Intent();
                            cbIntent.setClass(MainActivity.this, GmailService.class);
                            startService(cbIntent);

                            Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
                            startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(startIntent);
                                finish();
                            } else {
                                startService(startIntent);
                                finish();
                            }
                        } else{
                            checkPermissions();
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel(
                                        new DialogInterface.OnClickListener() {
                                            @TargetApi(Build.VERSION_CODES.M)
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //add these lines when uncommented
                                                //READ_SMS,READ_CALL_LOG,
                                                requestPermissions(new String[]{READ_CONTACTS,
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
            case 5000:
                int permissionLocation = ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    getMyLocation();
                }
                break;

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
            dialog = null;
        }
        stopLocationUpdates();
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
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getMyLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getApplicationContext(),"Please enable location to proceed further.",Toast.LENGTH_LONG).show();
                        finish();
                        break;
                }
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

                    if (!checkRuntimePermission()) {
                        requestRuntimePermission();
                    } else if(LocationReceiver.checkIfLocEnabled(MainActivity.this)){
                        Intent cbIntent =  new Intent();
                        cbIntent.setClass(MainActivity.this, GmailService.class);
                        startService(cbIntent);

                        Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
                        startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(startIntent);
                            finish();
                        } else {
                            startService(startIntent);
                            finish();
                        }
                    } else{
                        checkPermissions();
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
        Account[] accounts = AccountManager.get(MainActivity.this).getAccountsByType("com.google");
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

    private synchronized void setUpGClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                //.enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (hasPermission()){
            if(!checkNotificationAccess()){
                requestNotifictnAccess();
            }else{

                googleAccessAuthorise();
               /* if (!checkRuntimePermission()) {
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
                }*/
            }
        }else{
            requestAppUsagePermission();
        }
        //checkRuntimePermission();
    }

    private void getMyLocation(){
        if(mGoogleApiClient!=null) {
            if (mGoogleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mLocation =  LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(3000);
                    locationRequest.setFastestInterval(3000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(mGoogleApiClient, locationRequest, this);
                    PendingResult<LocationSettingsResult> result =
                            LocationServices.SettingsApi
                                    .checkLocationSettings(mGoogleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied.
                                    // You can initialize location requests here.
                                    int permissionLocation = ContextCompat
                                            .checkSelfPermission(ctx,
                                                    Manifest.permission.ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                        mLocation = LocationServices.FusedLocationApi
                                                .getLastLocation(mGoogleApiClient);

                                        Toast.makeText(MainActivity.this,"Location ON",Toast.LENGTH_LONG).show();
                                        Intent cbIntent =  new Intent();
                                        cbIntent.setClass(MainActivity.this, GmailService.class);
                                        startService(cbIntent);

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
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied.
                                    // But could be fixed by showing the user a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        // Ask to turn on GPS automatically
                                        status.startResolutionForResult(MainActivity.this,
                                                REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied.
                                    // However, we have no way
                                    // to fix the
                                    // settings so we won't show the dialog.
                                    // finish();
                                    break;
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        if (mLocation != null) {
            double latitude=mLocation.getLatitude();
            double longitude=mLocation.getLongitude();
        }
    }

    private void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        long UPDATE_INTERVAL = 5000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        long FASTEST_INTERVAL = 5000;
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(ctx.getApplicationContext(), "Enable Permissions", Toast.LENGTH_LONG).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    public void stopLocationUpdates()
    {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void checkPermissions(){
        int permissionLocation = ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 5000);
            }
        }else{
            getMyLocation();
        }

    }

}
