package com.ezeia.devicesensing.service.awstask;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisFirehoseRecorder;
import com.amazonaws.regions.Regions;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.ezeia.devicesensing.MainActivity;
import com.ezeia.devicesensing.R;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.AuthPreferences;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.service.GmailService;
import com.ezeia.devicesensing.utils.CellTower.CellTowerStateListener;
import com.ezeia.devicesensing.utils.NetworkConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import io.fabric.sdk.android.Fabric;
import static android.content.Context.TELEPHONY_SERVICE;

public class AwsUploader implements NetworkConnection.ResultListener,KinesisUploadTest.SubmmittedListener {

    private final Context ctx;
    private KinesisFirehoseRecorder firehoseRecorder;

    public AwsUploader(Context ctx) {

        this.ctx = ctx;
        Fabric.with(ctx, new Crashlytics());
        initRecorder();
    }

    private void initRecorder() {
        String cognitoIdentityPoolId = ctx.getString(R.string.cognito_identity_pool_id);
        Regions region = Regions.fromName(ctx.getString(R.string.region));

        File directory = ctx.getApplicationContext().getCacheDir();
        //BasicAWSCredentials credentials = new BasicAWSCredentials(ctx.getString(R.string.access_key),ctx.getString(R.string.secret_key));
        AWSCredentialsProvider provider = new CognitoCachingCredentialsProvider(
                ctx.getApplicationContext(),
                cognitoIdentityPoolId,
                region);

        // Create Kinesis recorders
        //KinesisRecorder kinesisRecorder = new KinesisRecorder(directory, region, provider);
        firehoseRecorder = new KinesisFirehoseRecorder(directory, region, provider);
    }

    /**
     * Submit a record to Kinesis Stream
     */
    /*public void submitKinesisRecord() {
        String kinesisStreamName = ctx.getString(R.string.kinesis_stream_name);

        JSONObject finalObject;
        List<String> jsonList = null;

        String newLineAdd;
        try {
            //jsonList = DatabaseInitializer.fetchFinalJsonData(AppDatabase.getAppDatabase(ctx));
                for(String data: jsonList){
                    uniqueID = DatabaseInitializer.fetchPrimaryID(AppDatabase.getAppDatabase(ctx),data);
                    if(Preference.getInstance(ctx) != null){
                        Preference.getInstance(ctx).put(Preference.Key.UNIQUE_ID,uniqueID);
                    }
                    finalObject = new JSONObject(data);
                    newLineAdd = finalObject.toString() + "\n";
                    firehoseRecorder.saveRecord(newLineAdd, kinesisStreamName);

                    AwsUploader uploader = new AwsUploader(ctx);
                    NetworkConnection connection = new NetworkConnection(uploader);
                    connection.execute();

                    int maxLogSize = 1000;
                    for (int i = 0; i <= finalObject.toString().length() / maxLogSize; i++) {
                        int start = i * maxLogSize;
                        int end = (i + 1) * maxLogSize;
                        end = end > finalObject.toString().length() ? finalObject.toString().length() : end;
                        Log.i("SUBSTRING JSON", finalObject.toString().substring(start, end));
                    }
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/

    //will not be called for now
    @Override
    public void isInternetConnected(Boolean aVoid) {

        if(aVoid){
            if(Preference.getInstance(ctx) != null){
                Preference.getInstance(ctx).getUniqueID();
            }
            KinesisUploadTest task = new KinesisUploadTest(firehoseRecorder,this);
            task.execute();
        }else{
            //String[] probeList = Constants.probeList;
            //DatabaseInitializer.deleteProbeByList(AppDatabase.getAppDatabase(ctx),probeList);
            DatabaseInitializer.deleteAllData(AppDatabase.getAppDatabase(ctx));

            Preference.getInstance(ctx).remove(Preference.Key.SCREEN_ON_TIME,Preference.Key.SCREEN_OFF_TIME,
                    Preference.Key.BATTERY_PLUGGED,Preference.Key.START_TIME,Preference.Key.CLOSE_TIME,
                    Preference.Key.PACKAGE_NAME,Preference.Key.IS_DEVICE_INFO,Preference.Key.ACC_X,
                    Preference.Key.ACC_Y,Preference.Key.ACC_Z,Preference.Key.ACCURACY,Preference.Key.CELL_TOWER_SIM,
                    Preference.Key.CELL_TOWER_VAL,Preference.Key.LOC_LATITUDE,Preference.Key.LOC_LONGITUDE,
                    Preference.Key.LOC_ALTITUDE,Preference.Key.LOC_ACCURACY,Preference.Key.LOC_SPEED,
                    Preference.Key.LOC_BEARING,Preference.Key.LOC_HAS_ALTITUDE,Preference.Key.LOC_HAS_ACCURACY,
                    Preference.Key.LOC_HAS_BEARING,Preference.Key.LOC_HAS_SPEED,Preference.Key.LOC_MOCK_PROVIDER,
                    Preference.Key.LOC_PROVIDER,Preference.Key.LOC_ELAPSED_TIME);
        }
    }

    public void submitKinesisRecordTest() {
        String kinesisStreamName = ctx.getString(R.string.kinesis_stream_name);

        JSONObject finalObject;
        String jsonList;
        String newLineAdd;
        Boolean isSaved;
        try {
            jsonList = DatabaseInitializer.fetchFinalJsonData(AppDatabase.getAppDatabase(ctx));
            if(jsonList != null) {
                finalObject = new JSONObject(jsonList);
                newLineAdd = finalObject.toString() + "\n";

                try {
                    firehoseRecorder.saveRecord(newLineAdd, kinesisStreamName);
                    isSaved = true;
                    ForegroundService.isReportSending = false;
                    Log.i(ForegroundService.LOG_TAG,"SUCCESSFULLY SAVING....");
                    Answers.getInstance().logCustom(new CustomEvent("Data Saving")
                            .putCustomAttribute("Exception","Saved"));

                } catch (AmazonClientException e) {
                    isSaved = false;
                    Log.i(ForegroundService.LOG_TAG,"ERROR IN SAVING...."+e);
                    Answers.getInstance().logCustom(new CustomEvent("Data Saving")
                            .putCustomAttribute("Exception",e.getMessage()));
                } catch (IllegalArgumentException e) {
                    isSaved = false;
                    Log.i(ForegroundService.LOG_TAG,"ERROR IN SAVING...."+e);
                    Answers.getInstance().logCustom(new CustomEvent("Data Saving")
                            .putCustomAttribute("Exception",e.getMessage()));
                } catch (Exception e) {
                    isSaved = false;
                    Log.i(ForegroundService.LOG_TAG,"ERROR IN SAVING...."+e);
                    Answers.getInstance().logCustom(new CustomEvent("Data Saving")
                            .putCustomAttribute("Exception",e.getMessage()));
                }

                if (isSaved) {
                    JSONArray jsonObjectUsage = DatabaseInitializer.fetchJsonArray(AppDatabase.getAppDatabase(ctx),"AppUsage");
                    if(jsonObjectUsage != null && jsonObjectUsage.length() < 1){
                        Log.i(ForegroundService.LOG_TAG,"APP USAGE WAS BLANK");
                        DatabaseInitializer.deleteAllData(AppDatabase.getAppDatabase(ctx));
                        Preference.getInstance(ctx).remove(Preference.Key.SCREEN_ON_TIME, Preference.Key.SCREEN_OFF_TIME,
                                Preference.Key.BATTERY_PLUGGED, Preference.Key.CLOSE_TIME,
                                Preference.Key.IS_DEVICE_INFO, Preference.Key.ACC_X,
                                Preference.Key.ACC_Y, Preference.Key.ACC_Z, Preference.Key.ACCURACY, Preference.Key.CELL_TOWER_SIM,
                                Preference.Key.CELL_TOWER_VAL, Preference.Key.LOC_LATITUDE, Preference.Key.LOC_LONGITUDE,
                                Preference.Key.LOC_ALTITUDE, Preference.Key.LOC_ACCURACY, Preference.Key.LOC_SPEED,
                                Preference.Key.LOC_BEARING, Preference.Key.LOC_HAS_ALTITUDE, Preference.Key.LOC_HAS_ACCURACY,
                                Preference.Key.LOC_HAS_BEARING, Preference.Key.LOC_HAS_SPEED, Preference.Key.LOC_MOCK_PROVIDER,
                                Preference.Key.LOC_PROVIDER, Preference.Key.LOC_ELAPSED_TIME);

                    }else{
                        Log.i(ForegroundService.LOG_TAG,"APP USAGE HAD DATA");
                        DatabaseInitializer.deleteAllData(AppDatabase.getAppDatabase(ctx));
                        Preference.getInstance(ctx).remove(Preference.Key.SCREEN_ON_TIME, Preference.Key.SCREEN_OFF_TIME,
                                Preference.Key.BATTERY_PLUGGED, Preference.Key.START_TIME, Preference.Key.CLOSE_TIME,
                                Preference.Key.PACKAGE_NAME, Preference.Key.IS_DEVICE_INFO, Preference.Key.ACC_X,
                                Preference.Key.ACC_Y, Preference.Key.ACC_Z, Preference.Key.ACCURACY, Preference.Key.CELL_TOWER_SIM,
                                Preference.Key.CELL_TOWER_VAL, Preference.Key.LOC_LATITUDE, Preference.Key.LOC_LONGITUDE,
                                Preference.Key.LOC_ALTITUDE, Preference.Key.LOC_ACCURACY, Preference.Key.LOC_SPEED,
                                Preference.Key.LOC_BEARING, Preference.Key.LOC_HAS_ALTITUDE, Preference.Key.LOC_HAS_ACCURACY,
                                Preference.Key.LOC_HAS_BEARING, Preference.Key.LOC_HAS_SPEED, Preference.Key.LOC_MOCK_PROVIDER,
                                Preference.Key.LOC_PROVIDER, Preference.Key.LOC_ELAPSED_TIME);
                    }

                    KinesisUploadTest task = new KinesisUploadTest(firehoseRecorder,this);
                    task.execute();
                }

                int maxLogSize = 1000;
                for (int i = 0; i <= finalObject.toString().length() / maxLogSize; i++) {
                    int start = i * maxLogSize;
                    int end = (i + 1) * maxLogSize;
                    end = end > finalObject.toString().length() ? finalObject.toString().length() : end;
                    Log.i("SUBSTRING JSON", finalObject.toString().substring(start, end));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void isSuccessfullySubmitted(Boolean aVoid) {
        if(Preference.getInstance(ctx) != null){
            Preference.getInstance(ctx).put(Preference.Key.IS_REPORT_SENT,true);
        }
        TelephonyManager mTelephonyManager = (TelephonyManager) ctx.getSystemService(TELEPHONY_SERVICE);
        if(mTelephonyManager != null)
            mTelephonyManager.listen(new CellTowerStateListener(ctx), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        Intent cbIntent =  new Intent();
        cbIntent.putExtra("comingFrom","Report");
        cbIntent.setClass(ctx, GmailService.class);
        ctx.startService(cbIntent);
    }
}
