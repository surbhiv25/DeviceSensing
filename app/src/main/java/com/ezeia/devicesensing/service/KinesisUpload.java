package com.ezeia.devicesensing.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisFirehoseRecorder;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.Preference;

class KinesisUpload extends AsyncTask<String,Void,String>{

    private final Context ctx;
    private final KinesisFirehoseRecorder recorder;

    public KinesisUpload(Context ctx, KinesisFirehoseRecorder firehoseRecorder) {
        this.ctx = ctx;
        this.recorder = firehoseRecorder;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Answers.getInstance().logCustom(new CustomEvent("AWS Called...PreExecute"));
    }

    @Override
    protected String doInBackground(String... v) {
        String uniqueID;
        try {
            Answers.getInstance().logCustom(new CustomEvent("AWS Called...DoInBackground"));
            uniqueID = v[0];

            recorder.submitAllRecords();
            DatabaseInitializer.updateFlag(AppDatabase.getAppDatabase(ctx),"1",uniqueID);

        } catch (AmazonClientException ace) {
            Answers.getInstance().logCustom(new CustomEvent("AWS Called...DoInBackground")
                    .putCustomAttribute("Exception",ace.getCause().toString()));

            Log.i("TAG", "kinesis.submitAll failed");
            Log.i("TAG",ace.getMessage()+"---"+ace.getCause());
            uniqueID = v[0];
            DatabaseInitializer.updateFlag(AppDatabase.getAppDatabase(ctx),"0",uniqueID);
            //initRecorder();
        }
        return uniqueID;
    }

    @Override
    protected void onPostExecute(String aVoid) {
        super.onPostExecute(aVoid);

        if(DatabaseInitializer.checkSubmitFlag(AppDatabase.getAppDatabase(ctx),aVoid)){
            Answers.getInstance().logCustom(new CustomEvent("AWS Called...PostExecute")
                    .putCustomAttribute("FlagValue","1"));

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
        }else{
            Answers.getInstance().logCustom(new CustomEvent("AWS Called...PostExecute")
                    .putCustomAttribute("FlagValue","0"));
        }
    }
}
