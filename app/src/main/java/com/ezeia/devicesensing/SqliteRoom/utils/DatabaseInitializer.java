package com.ezeia.devicesensing.SqliteRoom.utils;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.entity.DataFetch;
import com.ezeia.devicesensing.service.ForegroundService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;

public class DatabaseInitializer {

    private static final String TAG = DatabaseInitializer.class.getName();

    public static void populateAsync(@NonNull final AppDatabase db) {
        PopulateDbAsync task = new PopulateDbAsync(db);
        task.execute();
    }

   /* public static void populateSync(@NonNull final AppDatabase db) {
        //populateWithTestData(db);
    }*/

   /* private static DataFetch addUser(final AppDatabase db, DataFetch user) {
        db.userDao().insertAll(user);
        return user;
    }*/

    private static void addUser(final AppDatabase db, DataFetch user) {
        db.userDao().insertAll(user);
    }

    public static void deleteProbe(final AppDatabase db, String probeName) {
        db.userDao().deleteByName(probeName);
    }

    public static void deleteProbeByList(final AppDatabase db, String[] probeName) {
        for(int i=0;i<probeName.length;i++){
            db.userDao().deleteByName(probeName[i]);
        }
    }

    public static void deleteAllData(final AppDatabase db) {
        db.userDao().delete();
    }

    public static void updateFlag(final AppDatabase db,String submitFlg,String uniqueID) {
        db.userDao().update(submitFlg,"FINAL_JSON",uniqueID);
    }

    public static void addData(AppDatabase db, String probeName, String probeInfo, String timeStamp) {
        DataFetch user = new DataFetch();
        user.setProbeName(probeName);
        user.setProbeInfo(probeInfo);
        user.setTimeStamp(timeStamp);
        addUser(db, user);

        //List<DataFetch> userList = db.userDao().getAll(probeName);
        //for(DataFetch user1 : userList)
        //{
            //Log.i(ForegroundService.LOG_TAG, "SAVED DATA..." +"PROBE NAME: "+user1.getProbeName()
                   // +"\nPROBE INFO: "+user1.getProbeInfo()+"\nTIMESTAMP: "+user1.getTimeStamp());
        //}
    }

    public static boolean checkSubmitFlag(final AppDatabase db,String uniqueID) {
        Boolean flag = false;
        String flagVal = db.userDao().getSubmitFlag("FINAL_JSON",uniqueID);
        if(!TextUtils.isEmpty(flagVal))
        {
            flag = flagVal.equals("1");
        }
        return flag;
    }

    public static void addDataWithFlag(AppDatabase db, String probeName, String probeInfo, String timeStamp,String submitFlag) {
        DataFetch user = new DataFetch();
        user.setProbeName(probeName);
        user.setProbeInfo(probeInfo);
        user.setTimeStamp(timeStamp);
        //user.setSubmitFlag(submitFlag);
        addUser(db, user);

        //List<DataFetch> userList = db.userDao().getAll(probeName);
        //for(DataFetch user1 : userList)
        //{
        //Log.i(ForegroundService.LOG_TAG, "SAVED DATA..." +"PROBE NAME: "+user1.getProbeName()
        // +"\nPROBE INFO: "+user1.getProbeInfo()+"\nTIMESTAMP: "+user1.getTimeStamp());
        //}
    }

    public static org.json.JSONArray fetchJsonArray(AppDatabase db, String probeName) throws JSONException {

        List<String> userList = db.userDao().findByName(probeName);
        JSONArray header = new JSONArray();
            int i = 0;
            for(String entry: userList){
                Object json = new JSONTokener(entry).nextValue();
                if (json instanceof JSONObject){
                    JSONObject object = new JSONObject(entry);
                    header.put(object);
                } else if (json instanceof JSONArray){
                    JSONArray object = new JSONArray(entry);
                    if(object.length() >= 1){
                        for(int j = 0; j < object.length(); j++) {
                            JSONObject jsonObject = (JSONObject) object.get(j);
                            header.put(jsonObject);
                        }
                    }
                }
        }

        Log.i(ForegroundService.LOG_TAG, "FETCHED DATA..." +"PROBE NAME: "+probeName+"\nPROBE INFO: "+header);
        return header;
    }

    public static org.json.JSONObject fetchJsonData(AppDatabase db, String probeName) throws JSONException {

        JSONObject object;

        String userList = db.userDao().findSingleDataByName(probeName);
        if(userList == null){
            Log.i(ForegroundService.LOG_TAG, "FETCHED DATA..." +"PROBE NAME: "+probeName+"\nPROBE INFO: "+"No data available");
            object = new JSONObject();
            //object.put(probeName,"");
        }else{
            object = new JSONObject(userList);
            Log.i(ForegroundService.LOG_TAG, "FETCHED DATA..." +"PROBE NAME: "+probeName+"\nPROBE INFO: "+object);
        }

        return object;
    }

    public static String fetchFinalJsonData(AppDatabase db) {

        return db.userDao().findFinalDataByName("FINAL_JSON");
    }

    public static String fetchPrimaryID(AppDatabase db,String probeData) {

        return db.userDao().getPrimaryID(probeData);
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final AppDatabase mDb;

        PopulateDbAsync(AppDatabase db) {
            mDb = db;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            //populateWithTestData(mDb);
            return null;
        }

    }
}
