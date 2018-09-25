package com.ezeia.devicesensing.service;

import android.os.AsyncTask;
import android.util.Log;
import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisFirehoseRecorder;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

class KinesisUploadTest extends AsyncTask<Void,Void,Boolean>{

    private final KinesisFirehoseRecorder recorder;

    public KinesisUploadTest(KinesisFirehoseRecorder firehoseRecorder) {
        this.recorder = firehoseRecorder;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Answers.getInstance().logCustom(new CustomEvent("AWS Called...PreExecute"));
    }

    @Override
    protected Boolean doInBackground(Void... v) {
        try {
            Answers.getInstance().logCustom(new CustomEvent("AWS Called...DoInBackground"));
            recorder.submitAllRecords();
        } catch (AmazonClientException ace) {
            Answers.getInstance().logCustom(new CustomEvent("AWS Called...DoInBackground")
                    .putCustomAttribute("Exception",ace.getCause().toString()));

            Log.i("TAG",ace.getMessage()+"---"+ace.getCause());
            return true;
        } catch (Exception ace) {
            Answers.getInstance().logCustom(new CustomEvent("AWS Called...DoInBackground")
                    .putCustomAttribute("Exception",ace.getCause().toString()));
            Log.i("TAG",ace.getMessage()+"---"+ace.getCause());
            return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);

        if(aVoid){
            Answers.getInstance().logCustom(new CustomEvent("AWS Called...PostExecute")
                    .putCustomAttribute("FlagValue","Some error"));
        }else{
            Answers.getInstance().logCustom(new CustomEvent("AWS Called...PostExecute")
                    .putCustomAttribute("FlagValue","Successful"));
        }
    }
}
