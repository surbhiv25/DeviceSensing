package com.ezeia.devicesensing.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisFirehoseRecorder;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisRecorder;
import com.amazonaws.regions.Regions;
import com.crashlytics.android.Crashlytics;
import com.ezeia.devicesensing.R;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.Preference;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class AwsUploader {

    private final Context ctx;
    private KinesisRecorder kinesisRecorder;
    private KinesisFirehoseRecorder firehoseRecorder;

    //private KinesisProducer kinesisProducer = null;

    public AwsUploader(Context ctx) {

        this.ctx = ctx;
        Fabric.with(ctx, new Crashlytics());
        initRecorder();
    }

    private void initRecorder() {
        String cognitoIdentityPoolId = ctx.getString(R.string.cognito_identity_pool_id);
        Regions region = Regions.fromName(ctx.getString(R.string.region));

        // Get credential from Cognito Identiry Pool
        File directory = ctx.getApplicationContext().getCacheDir();
        BasicAWSCredentials credentials = new BasicAWSCredentials(ctx.getString(R.string.access_key),ctx.getString(R.string.secret_key));
        AWSCredentialsProvider provider = new CognitoCachingCredentialsProvider(
                ctx.getApplicationContext(),
                cognitoIdentityPoolId,
                region);

        // Create Kinesis recorders
        kinesisRecorder = new KinesisRecorder(directory, region, provider);
        firehoseRecorder = new KinesisFirehoseRecorder(directory, region, provider);
    }

    /**
     * Submit a record to Kinesis Stream
     */
    public void submitKinesisRecord(JSONObject jsonObject, final TextView textView) {
        String kinesisStreamName = ctx.getString(R.string.kinesis_stream_name);
        firehoseRecorder.saveRecord(jsonObject.toString(), kinesisStreamName);

        final String[] crashMessage = {""};

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... v) {
                    try {
                        firehoseRecorder.submitAllRecords();
                    } catch (AmazonClientException ace) {
                        Log.e("TAG", "kinesis.submitAll failed");
                        Log.e("TAG",ace.getMessage()+"---"+ace.getCause());
                        crashMessage[0] = ace.getMessage();
                        initRecorder();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);

                    textView.setText(crashMessage[0]);
                    //DatabaseInitializer.deleteAllData(AppDatabase.getAppDatabase(ctx));
                    /*Preference.getInstance(ctx).remove(Preference.Key.SCREEN_ON_TIME,Preference.Key.SCREEN_OFF_TIME,
                            Preference.Key.BATTERY_PLUGGED,Preference.Key.START_TIME,Preference.Key.CLOSE_TIME,
                            Preference.Key.PACKAGE_NAME,Preference.Key.IS_DEVICE_INFO,Preference.Key.ACC_X,
                            Preference.Key.ACC_Y,Preference.Key.ACC_Z,Preference.Key.ACCURACY,Preference.Key.CELL_TOWER_SIM,
                            Preference.Key.CELL_TOWER_VAL,Preference.Key.LOC_LATITUDE,Preference.Key.LOC_LONGITUDE,
                            Preference.Key.LOC_ALTITUDE,Preference.Key.LOC_ACCURACY,Preference.Key.LOC_SPEED,
                            Preference.Key.LOC_BEARING,Preference.Key.LOC_HAS_ALTITUDE,Preference.Key.LOC_HAS_ACCURACY,
                            Preference.Key.LOC_HAS_BEARING,Preference.Key.LOC_HAS_SPEED,Preference.Key.LOC_MOCK_PROVIDER,
                            Preference.Key.LOC_PROVIDER,Preference.Key.LOC_ELAPSED_TIME);*/
                }
            }.execute();
       /* } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * Save a record in Kinesis Firehose client (will not send cloud-side yet)
     *
     * @param put_string
     */
    public void saveFirehoseRecord(String put_string) {
        String firehoseStreamName = ctx.getString(R.string.kinesis_stream_name);
          /*  json.accumulate("time", sdf.format(new Date()));
            json.accumulate("model", Build.MODEL);
            json.accumulate("message", put_string);
            Log.e("TAG", json.toString());*/

            firehoseRecorder.saveRecord(put_string, firehoseStreamName);
    }

    /*private KinesisProducer getKinesisProducer(){
        if(kinesisProducer == null){

            KinesisProducerConfiguration config = new KinesisProducerConfiguration();
            config.setRegion(ctx.getString(R.string.region));
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(ctx.getString(R.string.access_key),ctx.getString(R.string.secret_key));
            config.setCredentialsProvider(new AWSStaticCredentialsProvider(awsCreds));
            config.setMaxConnections(1);
            config.setRequestTimeout(6000);
            config.setRecordMaxBufferedTime(5000);
            kinesisProducer = new KinesisProducer(config);
        }
        return kinesisProducer;
    }*/


}
