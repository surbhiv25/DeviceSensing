package com.ezeia.devicesensing.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisFirehoseRecorder;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisRecorder;
import com.amazonaws.regions.Regions;
import com.ezeia.devicesensing.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AwsUploader {

    private final Context ctx;
    private KinesisRecorder kinesisRecorder;
    private KinesisFirehoseRecorder firehoseRecorder;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.ENGLISH);

    public AwsUploader(Context ctx) {

        this.ctx = ctx;
        initRecorder();
    }

    private void initRecorder() {
        String cognitoIdentityPoolId = ctx.getString(R.string.cognito_identity_pool_id);
        Regions region = Regions.fromName(ctx.getString(R.string.region));

        // Get credential from Cognito Identiry Pool
        File directory = ctx.getApplicationContext().getCacheDir();
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

    public void submitKinesisRecord(JSONObject jsonObject) {
        String kinesisStreamName = ctx.getString(R.string.kinesis_stream_name);
        //JSONObject json = new JSONObject();
        //try {
          /*  json.accumulate("time", sdf.format(new Date()));
            json.accumulate("azimuth", azimuth);
            json.accumulate("pitch", pitch);
            json.accumulate("roll", roll);
            Log.e("TAG", json.toString());*/
            kinesisRecorder.saveRecord(jsonObject.toString(), kinesisStreamName);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... v) {
                    try {
                        kinesisRecorder.submitAllRecords();
                    } catch (AmazonClientException ace) {
                        Log.e("TAG", "kinesis.submitAll failed");
                        Log.e("TAG",ace.getMessage()+"---"+ace.getCause());
                        initRecorder();
                    }
                    return null;
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
        String firehoseStreamName = ctx.getString(R.string.firehose_stream_name);
        JSONObject json = new JSONObject();
        try {
            json.accumulate("time", sdf.format(new Date()));
            json.accumulate("model", Build.MODEL);
            json.accumulate("message", put_string);
            Log.e("TAG", json.toString());
            firehoseRecorder.saveRecord(json.toString(), firehoseStreamName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
