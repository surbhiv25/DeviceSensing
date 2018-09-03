package com.ezeia.devicesensing.utils.CellTower;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.ezeia.devicesensing.LogsUtil;
import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;
import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.utils.CommonFunctions;
import com.ezeia.devicesensing.utils.Functions;
import com.google.gson.JsonObject;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.TELEPHONY_SERVICE;

public class CellTowerStateListener extends PhoneStateListener
{
    private Context ctx;
    private Boolean quitLooper;

    public CellTowerStateListener(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);

        initializeTimerTask(signalStrength);
        //Looper.loop();
    }

    private void initializeTimerTask(final SignalStrength signalStrength) {

        final int signalStrengthValue;
        final String simType;

        if (signalStrength.isGsm()) {
            if (signalStrength.getGsmSignalStrength() != 99){
                signalStrengthValue = signalStrength.getGsmSignalStrength() * 2 - 113;
                simType = "GSM";
                //createJson("GSM",signalStrengthValue);
            }
            else{
                signalStrengthValue = signalStrength.getGsmSignalStrength();
                simType = "GSM";
                //createJson("GSM",signalStrengthValue);
            }
        } else {
            signalStrengthValue = signalStrength.getCdmaDbm();
            simType = "GSM";
            //createJson("CDMA",signalStrengthValue);
        }
        //Log.i("TAG","Signal Strength : " + signalStrengthValue);

        Boolean checkIfPluggedIn = Preference.getInstance(ctx).isCellTowerEmpty();
        if(checkIfPluggedIn) {
            Preference.getInstance(ctx).put(Preference.Key.CELL_TOWER_SIM, simType);
            Preference.getInstance(ctx).put(Preference.Key.CELL_TOWER_VAL, String.valueOf(signalStrengthValue));
        }

        /*
        TelephonyManager mTelephonyManager = (TelephonyManager) ctx.getSystemService(TELEPHONY_SERVICE);
        mTelephonyManager.listen(CellTowerStateListener.this, PhoneStateListener.LISTEN_NONE);*/
        /*  final Handler ha=new Handler();
        ha.postDelayed(new Runnable() {
            @Override
            public void run()
            {

            }
        }, 30000);*/

    }
}
