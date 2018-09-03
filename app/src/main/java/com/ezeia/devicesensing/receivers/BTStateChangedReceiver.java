package com.ezeia.devicesensing.receivers;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.service.ForegroundService;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.ezeia.devicesensing.utils.Functions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Pattern;

public class BTStateChangedReceiver extends BroadcastReceiver {

    private LinkedHashMap<String,Object> hashMap = new LinkedHashMap<>();
    private Context context;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        String action = intent.getAction();
        this.context = context;

        if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //Log.i(ForegroundService.LOG_TAG,"BLUETOOTH CONNECTED ACTION: "+ CommonFunctions.fetchDateInUTC());
            //Log.i(ForegroundService.LOG_TAG,"INFO: NAME- "+device.getName()+"\nADDRESS- "+device.getAddress()
                   // +"\nBOND STATE- "+device.getBondState()+"\nTYPE- "+device.getType());

            LinkedHashMap<String,String> hashMap = new LinkedHashMap<>();
            hashMap.put("connected", "true");
            hashMap.put("name", device.getName());
            hashMap.put("address", device.getAddress());
            hashMap.put("bond_State", String.valueOf(device.getBondState()));
            hashMap.put("type", String.valueOf(device.getType()));
            hashMap.put("timestamp", CommonFunctions.fetchDateInUTC());

            createJsonReportConnection(hashMap);

        }else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //Log.i(ForegroundService.LOG_TAG,"BLUETOOTH DISCONNECTED ACTION: "+ CommonFunctions.fetchDateInUTC());
            //Log.i(ForegroundService.LOG_TAG,"INFO: NAME- "+device.getName()+"\nADDRESS- "+device.getAddress()
                   // +"\nBOND STATE- "+device.getBondState()+"\nTYPE- "+device.getType());

            LinkedHashMap<String,String> hashMap = new LinkedHashMap<>();
            hashMap.put("connected", "false");
            hashMap.put("name", device.getName());
            hashMap.put("address", device.getAddress());
            hashMap.put("bond_State", String.valueOf(device.getBondState()));
            hashMap.put("type", String.valueOf(device.getType()));
            hashMap.put("timestamp", CommonFunctions.fetchDateInUTC());

            createJsonReportConnection(hashMap);

        }

        switch(state){
            case BluetoothAdapter.STATE_CONNECTED:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
               /* Log.i(ForegroundService.LOG_TAG,"BLUETOOTH CONNECTED: "+ CommonFunctions.fetchDateInUTC());
                Log.i(ForegroundService.LOG_TAG,"INFO: NAME- "+device.getName()+"\nADDRESS- "+device.getAddress()
                                +"\nBOND STATE- "+device.getBondState()+"\nTYPE- "+device.getType());
                */
                break;

            case BluetoothAdapter.STATE_CONNECTING:
                //Toast.makeText(context, "BTStateChangedBroadcastReceiver: STATE_CONNECTING", Toast.LENGTH_SHORT).show();
                break;

            case BluetoothAdapter.STATE_DISCONNECTED:
                BluetoothDevice deviceDisconnect = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
              /*  Log.i(ForegroundService.LOG_TAG,"BLUETOOTH DISCONNECTED: "+ CommonFunctions.fetchDateInUTC());
                Log.i(ForegroundService.LOG_TAG,"INFO: NAME- "+deviceDisconnect.getName()+"\nADDRESS- "+deviceDisconnect.getAddress()
                        +"\nBOND STATE- "+deviceDisconnect.getBondState()+"\nTYPE- "+deviceDisconnect.getType());
                */
                break;

            case BluetoothAdapter.STATE_DISCONNECTING:
                //Toast.makeText(context, "BTStateChangedBroadcastReceiver: STATE_DISCONNECTING", Toast.LENGTH_SHORT).show();
                break;

            case BluetoothAdapter.STATE_OFF: {
                Log.i(ForegroundService.LOG_TAG, "BLUETOOTH OFF: " + CommonFunctions.fetchDateInUTC());
                String nameState = checkBluetoothConn();
                //Log.i(ForegroundService.LOG_TAG, "NAME STATE: " + nameState);

                ArrayList<String> pairedDevices = getPairedDevices();
                //Log.i(ForegroundService.LOG_TAG, "PAIRED DEVICES: " + pairedDevices);

                LinkedHashMap<String,Object> hashMap = new LinkedHashMap<>();
                if (nameState.contains("^")) {
                    
                    String name = nameState.split(Pattern.quote("^"))[0];
                    String stateBluetooth = nameState.split(Pattern.quote("^"))[1];
                    hashMap.put("connection", "OFF");
                    hashMap.put("name", name);
                    hashMap.put("state", stateBluetooth);
                    hashMap.put("paired_Devices", pairedDevices);
                    hashMap.put("timestamp", CommonFunctions.fetchDateInUTC());
                } else {
                    //hashMap.put("connection", "OFF");
                    hashMap.put("name", nameState);
                    hashMap.put("paired_Devices", pairedDevices);
                    hashMap.put("timestamp", CommonFunctions.fetchDateInUTC());
                }
                createJsonReportState(hashMap);
                break;
            }

            case BluetoothAdapter.STATE_ON:
                //Log.i(ForegroundService.LOG_TAG,"BLUETOOTH ON: "+ CommonFunctions.fetchDateInUTC());

                String nameState = checkBluetoothConn();
                //Log.i(ForegroundService.LOG_TAG,"NAME STATE: "+nameState);

                ArrayList<String> pairedDevices = getPairedDevices();
                //Log.i(ForegroundService.LOG_TAG,"PAIRED DEVICES: "+pairedDevices);

                LinkedHashMap<String,Object> hashMap = new LinkedHashMap<>();
                if(nameState.contains("^")){
                    String name = nameState.split(Pattern.quote("^"))[0];
                    String stateBluetooth = nameState.split(Pattern.quote("^"))[1];
                    hashMap.put("connection","ON");
                    hashMap.put("name",name);
                    hashMap.put("state",stateBluetooth);
                    hashMap.put("paired_Devices", pairedDevices);
                    hashMap.put("timestamp",CommonFunctions.fetchDateInUTC());
                }else{
                    //hashMap.put("connection","ON");
                    hashMap.put("name",nameState);
                    hashMap.put("paired_Devices", pairedDevices);
                    hashMap.put("timestamp",CommonFunctions.fetchDateInUTC());
                }

                createJsonReportState(hashMap);
                //Toast.makeText(context, "BTStateChangedBroadcastReceiver: STATE_ON", Toast.LENGTH_SHORT).show();
                break;

            case BluetoothAdapter.STATE_TURNING_OFF:
                //Toast.makeText(context, "BTStateChangedBroadcastReceiver: STATE_TURNING_OFF", Toast.LENGTH_SHORT).show();
                break;

            case BluetoothAdapter.STATE_TURNING_ON:
                //Toast.makeText(context, "BTStateChangedBroadcastReceiver: STATE_TURNING_ON", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private static String checkBluetoothConn()
    {
        String buffer = "";

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter != null) //if null device does not support bluetooth
        {
            int state;
            String name;
            if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON)
            {
                state = mBluetoothAdapter.getState();
                name = mBluetoothAdapter.getName();
            }
            else
            {
                state = mBluetoothAdapter.getState();
                name = mBluetoothAdapter.getName();
            }
            buffer = name+"^"+state;
            return buffer;
        } else {
            buffer = "Bluetooth not supported";
            return buffer;
        }
    }

    private ArrayList<String> getPairedDevices()
    {
        ArrayList<String> buffer = new ArrayList<>();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) //if null device does not support bluetooth
        {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if(pairedDevices != null && pairedDevices.size() != 0)
            {
                for(BluetoothDevice bt : pairedDevices)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        buffer.add(bt.getName() +"^"+ bt.getType() +"^"+ bt.getBondState() +"^"+ bt.getAddress());
                    }
                }
            }
            else
            {
                buffer.add("No paired devices");
            }
        } else {
            buffer.add("Bluetooth not supported");
        }
        return buffer;
    }

    private void createJsonReportState(LinkedHashMap<String,Object> dataHashmap)
    {
        JsonObject subItems = new JsonObject();
        JSONObject header = new JSONObject();
        String connection,name,state,timestamp;
        ArrayList<String> pairedDevice = null;

        if(dataHashmap != null && dataHashmap.size() > 0){
            if(dataHashmap.containsKey("connection")){
               connection = dataHashmap.get("connection").toString();
                subItems.addProperty("connection",connection);
            }
            if(dataHashmap.containsKey("name")){
                name = dataHashmap.get("name").toString();
                subItems.addProperty("name",name);
            }
            if(dataHashmap.containsKey("state")){
                state = dataHashmap.get("state").toString();
                subItems.addProperty("state",state);
            }
            if(dataHashmap.containsKey("timestamp")){
                timestamp = dataHashmap.get("timestamp").toString();
                subItems.addProperty("timestamp",timestamp);
            }
            if(dataHashmap.containsKey("paired_Devices")){
                if(dataHashmap.get("paired_Devices") != null){
                    Object obj = dataHashmap.get("paired_Devices");
                    if(obj instanceof ArrayList<?>)
                        pairedDevice = (ArrayList<String>) dataHashmap.get("paired_Devices");
                }

                JsonArray jsonArray = new JsonArray();
                if(pairedDevice != null && pairedDevice.size() > 0)
                {
                    if(!pairedDevice.get(0).contains("^")){
                        JsonObject person = new JsonObject();
                        person.addProperty("name", pairedDevice.get(0));
                        jsonArray.add(person);
                    }else{
                        for(String entry : pairedDevice)
                        {
                            JsonObject person = new JsonObject();
                            person.addProperty("name", entry.split(Pattern.quote("^"))[0]);
                            person.addProperty("type", entry.split(Pattern.quote("^"))[1]);
                            person.addProperty("bond_state", entry.split(Pattern.quote("^"))[2]);
                            person.addProperty("address", entry.split(Pattern.quote("^"))[3]);
                            jsonArray.add(person);
                        }
                    }
                }
                subItems.add("paired_Devices",jsonArray);
                Functions functions = new Functions(context);
                JsonObject objectLoc = functions.fetchLocation();
                subItems.add("location",objectLoc);

            }
        }
        //DatabaseInitializer.deleteProbe(AppDatabase.getAppDatabase(context),"Bluetooth_State");
        DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"Bluetooth_State",subItems.toString(),CommonFunctions.fetchDateInUTC());
    }

    private void createJsonReportConnection(LinkedHashMap<String,String> dataHashmap)
    {
        JsonObject subItems = new JsonObject();
        String name,timestamp;
        
        if(dataHashmap != null && dataHashmap.size() > 0){
            if(dataHashmap.containsKey("connected")){
                String connected = dataHashmap.get("connected");
                subItems.addProperty("connected",connected);
            }
            if(dataHashmap.containsKey("name")){
                name = dataHashmap.get("name");
                subItems.addProperty("name",name);
            }
            if(dataHashmap.containsKey("address")){
                String address = dataHashmap.get("address");
                subItems.addProperty("address",address);
            }
            if(dataHashmap.containsKey("bond_State")){
                String bond_State = dataHashmap.get("bond_State");
                subItems.addProperty("bond_State",bond_State);
            }
            if(dataHashmap.containsKey("type")){
                String type = dataHashmap.get("type");
                subItems.addProperty("type",type);
            }
            if(dataHashmap.containsKey("timestamp")){
                timestamp = dataHashmap.get("timestamp");
                subItems.addProperty("timestamp",timestamp);
            }

            Functions functions = new Functions(context);
            JsonObject objectLoc = functions.fetchLocation();
            subItems.add("location",objectLoc);

            //DatabaseInitializer.deleteProbe(AppDatabase.getAppDatabase(context),"Bluetooth_Connection");
            //header.put("Bluetooth_Connection",subItems);
            //Log.i(ForegroundService.LOG_TAG,"JSONNN...."+subItems);
            DatabaseInitializer.addData(AppDatabase.getAppDatabase(context),"Bluetooth_Connection",subItems.toString(),CommonFunctions.fetchDateInUTC());
        }
    }
}