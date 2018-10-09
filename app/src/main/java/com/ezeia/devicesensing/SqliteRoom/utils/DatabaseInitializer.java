package com.ezeia.devicesensing.SqliteRoom.utils;

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

    private static void addUser(final AppDatabase db, DataFetch user) {
        db.userDao().insertAll(user);
    }

   /* public static void deleteProbe(final AppDatabase db, String probeName) {
        db.userDao().deleteByName(probeName);
    }*/

    public static void deleteAllData(final AppDatabase db) {
        db.userDao().delete();
    }

  /*  public static void updateFlag(final AppDatabase db,String submitFlg,String uniqueID) {
        db.userDao().update(submitFlg,"FINAL_JSON",uniqueID);
    }*/

    public static void addData(AppDatabase db, String probeName, String probeInfo, String timeStamp) {
        DataFetch user = new DataFetch();
        user.setProbeName(probeName);
        user.setProbeInfo(probeInfo);
        user.setTimeStamp(timeStamp);
        addUser(db, user);
    }

    public static void addDataWithFlag(AppDatabase db, String probeName, String probeInfo, String timeStamp) {
        DataFetch user = new DataFetch();
        user.setProbeName(probeName);
        user.setProbeInfo(probeInfo);
        user.setTimeStamp(timeStamp);
        addUser(db, user);
    }

    public static org.json.JSONArray fetchJsonArray(AppDatabase db, String probeName) throws JSONException {

        List<String> userList = db.userDao().findByName(probeName);
        JSONArray header = new JSONArray();
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

    public static org.json.JSONArray fetchSingleArray(AppDatabase db, String probeName) throws JSONException {

        String userList = db.userDao().findSingleDataByName(probeName);
        JSONArray object = null;
        if(userList != null){
            object = new JSONArray(userList);
            Log.i(ForegroundService.LOG_TAG, "FETCHED DATA..." +"PROBE NAME: "+probeName+"\nPROBE INFO: "+object);
        }
        return object;
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

}
