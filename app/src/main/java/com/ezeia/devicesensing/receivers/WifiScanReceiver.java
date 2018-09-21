package com.ezeia.devicesensing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.ezeia.devicesensing.pref.Preference;

import java.util.List;

public class WifiScanReceiver extends BroadcastReceiver {
    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //StringBuffer buffer = null;
        this.context = context;
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            if(!Preference.getInstance(context).getWifiScanInfo()){
               // buffer = getWifiName();
                Preference.getInstance(context).put(Preference.Key.IS_WIFI_SCANNED,true);
            }
        }

        //Log.i(ForegroundService.LOG_TAG,"AVAILABLE NETWORKS: "+buffer);
        //Toast.makeText(context,"wifi scanning", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private StringBuffer getWifiName() {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        StringBuffer bufferWifi =  new StringBuffer();
        if (manager.isWifiEnabled() ) {
                List<ScanResult> scans = manager.getScanResults();
                if(scans != null && !scans.isEmpty()){
                    int counter = 0;
                    for (ScanResult scan : scans) {
                        counter++;
                        String BSSID = scan.BSSID;
                        String SSID = scan.SSID;
                        String capabilities = scan.capabilities;
                        int frequency = scan.frequency;
                        int level =  scan.level;
                        Long timeStamp = scan.timestamp;

                        bufferWifi.append(counter).append(". BSSID: ").append(BSSID)
                                .append("\nSSID: ").append(SSID)
                                .append("\nCAPABILITIES: ").append(capabilities)
                                .append("\nFREQUENCY: ").append(frequency)
                                .append("\nLEVEL: ").append(level)
                                .append("\nTIMESTAMP: ").append(timeStamp)
                                .append("\n\n");
                    }
            }
        }
        else
        {
            bufferWifi.append("WIFI not enabled");
        }
        return bufferWifi;
    }

}