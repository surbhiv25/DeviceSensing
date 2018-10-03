package com.ezeia.devicesensing.utils.CellTower;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;

import com.ezeia.devicesensing.pref.Preference;
import com.ezeia.devicesensing.service.ForegroundService;

public class CellTowerStateListener extends PhoneStateListener
{
    private final Context ctx;

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
        Log.i(ForegroundService.LOG_TAG,"Signal LISTENER : " + signalStrengthValue);

        //Boolean checkIfPluggedIn = Preference.getInstance(ctx).isCellTowerEmpty();
        //if(checkIfPluggedIn) {
            Preference.getInstance(ctx).put(Preference.Key.CELL_TOWER_SIM, simType);
            Preference.getInstance(ctx).put(Preference.Key.CELL_TOWER_VAL, String.valueOf(signalStrengthValue));
        //}

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
