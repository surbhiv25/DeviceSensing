package com.ezeia.devicesensing.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.google.gson.JsonObject;


@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationService extends NotificationListenerService {

    Context context;

    @Override
    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pack = sbn.getPackageName();
        String ticker ="";
        Boolean isSame = true;

        String msgID;
        if(sbn.getNotification().tickerText !=null) {
            ticker = sbn.getNotification().tickerText.toString();
        }

        msgID = sbn.getKey();
        if(Preference.getInstance(context) != null)
            if((Preference.getInstance(context).isMsgTextEmpty() && Preference.getInstance(context).isMsgTitleEmpty()) ||
                    (!Preference.getInstance(context).getMsgText().equals(msgID)) &&
                            !Preference.getInstance(context).getMsgTitle().equals(pack)){
                Preference.getInstance(context).put(Preference.Key.MSG_TEXT,msgID);
                Preference.getInstance(context).put(Preference.Key.MSG_TITLE,pack);
                isSame = false;
        }else{
            isSame = true;
        }

        Bundle extras = sbn.getNotification().extras;
        String title = "",text = "";

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)){
            Parcelable b[] = (Parcelable[]) extras.get(Notification.EXTRA_MESSAGES);
            if(b != null){
                if(extras.getString("android.title") != null){
                    title = extras.getString("android.title");
                }

                text = "";
                for (Parcelable tmp : b){
                    Bundle msgBundle = (Bundle) tmp;
                    text = text + msgBundle.getString("text") + "\n";
                }
                if(!isSame){
                    Log.i("Package",pack);
                    Log.i("Ticker",ticker);
                    Log.i("Title",title);
                    Log.i("Text",text);

                    createJson(pack,title,ticker,text);
                }
            }else{
                if(extras.getString("android.title") != null){
                    title = extras.getString("android.title");
                }
                if(extras.getCharSequence("android.text") != null){
                    text = extras.getCharSequence("android.text").toString();
                }

                if(!isSame){
                    Log.i("Package",pack);
                    Log.i("Ticker",ticker);
                    Log.i("Title",title);
                    Log.i("Text",text);

                    createJson(pack,title,ticker,text);
                }
            }
        }else{
            if(extras.getString("android.title") != null){
                title = extras.getString("android.title");
            }
            if(extras.getCharSequence("android.text") != null){
                text = extras.getCharSequence("android.text").toString();
            }
             //id1 = extras.getInt(Notification.EXTRA_SMALL_ICON);

            if(!isSame){
                Log.i("Package",pack);
                Log.i("Ticker",ticker);
                Log.i("Title",title);
                Log.i("Text",text);

                createJson(pack,title,ticker,text);
            }
        }

        Intent msgrcv = new Intent("Msg");
        msgrcv.putExtra("package", pack);
        msgrcv.putExtra("ticker", ticker);
        msgrcv.putExtra("title", title);
        msgrcv.putExtra("text", text);

        //Log.i("TAGGG","Notification msg service..."+title+"--"+text+"--"+pack);
        /*if(id != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            id.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            msgrcv.putExtra("icon",byteArray);
        }*/
        LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        String pack = sbn.getPackageName();
        Boolean isSame = true;
        String msgID;
        msgID = sbn.getKey();
        if(Preference.getInstance(context) != null)
            if((Preference.getInstance(context).isMsgTextEmpty() && Preference.getInstance(context).isMsgTitleEmpty()) ||
                    (!Preference.getInstance(context).getMsgText().equals(msgID)) &&
                            !Preference.getInstance(context).getMsgTitle().equals(pack)){
                Preference.getInstance(context).put(Preference.Key.MSG_TEXT,msgID);
                Preference.getInstance(context).put(Preference.Key.MSG_TITLE,pack);
                isSame = false;
            }else{
                isSame = true;
            }

        if(!isSame){
            Log.i("Msg","Notification Removed..."+ sbn.getPackageName());
            createJson(sbn.getPackageName());
        }
    }

    private void createJson(String packgName, String title, String ticker, String text){
        JsonObject object = new JsonObject();
        object.addProperty("PackageName", packgName);
        object.addProperty("Title",title);
        object.addProperty("Ticker",ticker);
        object.addProperty("Text",text);
        object.addProperty("Status","Posted");
        object.addProperty("Timestamp",CommonFunctions.fetchDateInUTC());
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"Notification_Stats",object.toString(),CommonFunctions.fetchDateInUTC());
    }

    private void createJson(String packgName){
        JsonObject object = new JsonObject();
        object.addProperty("PackageName", packgName);
        object.addProperty("Status","Cleared");
        object.addProperty("Timestamp",CommonFunctions.fetchDateInUTC());
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"Notification_Stats",object.toString(),CommonFunctions.fetchDateInUTC());
    }
}
