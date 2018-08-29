package com.ezeia.devicesensing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.utils.CommonFunctions;

public class CameraEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Cursor cursor = context.getContentResolver().query(intent.getData(), null,null, null, null);
        cursor.moveToFirst();
        String image_path = cursor.getString(cursor.getColumnIndex("_data"));
        cursor.close();
        Log.i(ForegroundService.LOG_TAG,"CAMERA CLICKED: "+ image_path);
        Log.i(ForegroundService.LOG_TAG,"CAMERA CLICK TIME: "+ CommonFunctions.fetchDateInUTC());
        Toast.makeText(context,"Camera image clicked",Toast.LENGTH_SHORT).show();
    }

}
