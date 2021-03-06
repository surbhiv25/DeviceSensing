package com.ezeia.devicesensing.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class Preference {
    private static final String SETTINGS_NAME = "login_pref";
    private static Preference sSharedPrefs;
    private final SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private boolean mBulkUpdate = false;

    /**
     * Class for keeping all the keys used for shared preferences in one place.
     */
    public static class Key {
        public static final String BATTERY_PLUGGED = "battery_plugged";

        public static final String PACKAGE_NAME = "package_name";
        public static final String START_TIME = "start_time";
        public static final String CLOSE_TIME = "close_time";

        public static final String SCREEN_ON_TIME = "screen_on_time";
        public static final String SCREEN_OFF_TIME = "screen_off_time";

        public static final String IS_DEVICE_INFO = "deviceInfo";
        public static final String IS_WIFI_SCANNED = "wifiScan";

        public static final String ACC_X= "AccX";
        public static final String ACC_Y = "AccY";
        public static final String ACC_Z = "AccZ";
        public static final String ACCURACY = "Accuracy";

        public static final String CELL_TOWER_SIM = "cell_tower_sim";
        public static final String CELL_TOWER_VAL = "cell_tower_strength";

        public static final String FIRST_REPORT = "first_report";

        public static final String LOC_LATITUDE = "latitude";
        public static final String LOC_LONGITUDE = "longitude";
        public static final String LOC_ALTITUDE = "altitude";
        public static final String LOC_ACCURACY = "accuracy";
        public static final String LOC_SPEED = "speed";
        public static final String LOC_BEARING = "bearing";
        public static final String LOC_HAS_ACCURACY = "has_accuracy";
        public static final String LOC_HAS_ALTITUDE = "has_altitude";
        public static final String LOC_HAS_SPEED = "has_speed";
        public static final String LOC_HAS_BEARING = "has_bearing";
        public static final String LOC_MOCK_PROVIDER = "is_mock_provider";
        public static final String LOC_PROVIDER = "provider";
        public static final String LOC_ELAPSED_TIME = "elapsed_time";

        public static final String UNIQUE_ID = "unique_id";

        public static final String IS_HANDLER_CALLED = "is_handler_called";
        public static final String IS_REPORT_SENT = "is_report_sent";

        public static final String MSG_TEXT = "msg_text";
        public static final String MSG_TITLE = "msg_title";

        public static final String ALARM_SENDING = "alarm_send";
    }

    private Preference(Context context) {
        mPref = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
    }

    public static Preference getInstance(Context context) {
        if (sSharedPrefs == null) {
            synchronized (Preference.class) {
                if (sSharedPrefs == null) {
                    sSharedPrefs = new Preference(context.getApplicationContext());
                }
            }
        }
        return sSharedPrefs;
    }

    public void put(String key, String val) {
        doEdit();
        mEditor.putString(key, val);
        doCommit();
    }

    public void put(String key, int val) {
        doEdit();
        mEditor.putInt(key, val);
        doCommit();
    }

    public void put(String key, boolean val) {
        doEdit();
        mEditor.putBoolean(key, val);
        doCommit();
    }

    public void put(String key, float val) {
        doEdit();
        mEditor.putFloat(key, val);
        doCommit();
    }

    public void put(String key, double val) {
        doEdit();
        mEditor.putString(key, String.valueOf(val));
        doCommit();
    }

    public void put(String key, long val) {
        doEdit();
        mEditor.putLong(key, val);
        doCommit();
    }

    public String getString(String key, String defaultValue) {
        return mPref.getString(key, defaultValue);
    }

    private String getString(String key) {
        return mPref.getString(key, null);
    }

    public int getInt(String key) {
        return mPref.getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return mPref.getInt(key, defaultValue);
    }

    public long getLong(String key) {
        return mPref.getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        return mPref.getLong(key, defaultValue);
    }

    public float getFloat(String key) {
        return mPref.getFloat(key, 0);
    }

    public float getFloat(String key, float defaultValue) {
        return mPref.getFloat(key, defaultValue);
    }

    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    private double getDouble(String key, double defaultValue) {
        try {
            return Double.valueOf(mPref.getString(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mPref.getBoolean(key, defaultValue);
    }

    private boolean getBoolean(String key) {
        return mPref.getBoolean(key, false);
    }

    public void remove(String... keys) {
        doEdit();
        for (String key : keys) {
            mEditor.remove(key);
        }
        doCommit();
    }

    public void clear() {
        doEdit();
        mEditor.clear();
        doCommit();
    }

    public void edit() {
        mBulkUpdate = true;
        mEditor = mPref.edit();
    }

    public void commit() {
        mBulkUpdate = false;
        mEditor.commit();
        mEditor = null;
    }

    private void doEdit() {
        if (!mBulkUpdate && mEditor == null) {
            mEditor = mPref.edit();
        }
    }

    private void doCommit() {
        if (!mBulkUpdate && mEditor != null) {
            mEditor.commit();
            mEditor = null;
        }
    }

    public boolean isBatteryPlugged() {
        return TextUtils.isEmpty(getString(Key.BATTERY_PLUGGED));
    }

    public boolean isPackageNameEmpty() {
        return TextUtils.isEmpty(getString(Key.PACKAGE_NAME));
    }

    private boolean isStartTimeEmpty() {
        return TextUtils.isEmpty(getString(Key.START_TIME));
    }

    public String getStartTime() {
        return isStartTimeEmpty() ? "" : getString(Key.START_TIME);
    }

    private boolean isCloseTimeEmpty() {
        return TextUtils.isEmpty(getString(Key.CLOSE_TIME));
    }

    public String getCloseTime() {
        return isCloseTimeEmpty() ? "" : getString(Key.CLOSE_TIME);
    }

    public String getBatteryPlugStatus() {
        return isBatteryPlugged() ? "" : getString(Key.BATTERY_PLUGGED);
    }

    public String getPackageName() {
        return isPackageNameEmpty() ? "" : getString(Key.PACKAGE_NAME);
    }

    private boolean isDeviceInfoEmpty() {
        return getBoolean(Key.IS_DEVICE_INFO);
    }

    public Boolean getDeviceInfo() {
        return !isDeviceInfoEmpty() && getBoolean(Key.IS_DEVICE_INFO);
    }

    private boolean isWifiScanEmpty() {
        return getBoolean(Key.IS_WIFI_SCANNED);
    }

    public Boolean getWifiScanInfo() {
        return !isWifiScanEmpty() && getBoolean(Key.IS_WIFI_SCANNED);
    }

    private boolean isAccXEmpty() {
        return TextUtils.isEmpty(getString(Key.ACC_X));
    }
    public String getAccX() {
        return isAccXEmpty() ? "" : getString(Key.ACC_X);
    }

    private boolean isAccYEmpty() {
        return TextUtils.isEmpty(getString(Key.ACC_Y));
    }
    public String getAccY() {
        return isAccYEmpty() ? "" : getString(Key.ACC_Y);
    }

    private boolean isAccZEmpty() {
        return TextUtils.isEmpty(getString(Key.ACC_Z));
    }
    public String getAccZ() {
        return isAccZEmpty() ? "" : getString(Key.ACC_Z);
    }

    private boolean isAccuracyEmpty() {
        return TextUtils.isEmpty(getString(Key.ACCURACY));
    }
    public String getAccuracy() {
        return !isAccuracyEmpty() ? "" : getString(Key.ACCURACY);
    }

    private boolean isCellTowerEmpty() {
        return TextUtils.isEmpty(getString(Key.CELL_TOWER_SIM));
    }
    public String getCellTower() {
        return isCellTowerEmpty() ? "" : getString(Key.CELL_TOWER_SIM);
    }

    public String getCellTowerStrength() {
        return isCellTowerEmpty() ? "" : getString(Key.CELL_TOWER_VAL);
    }

    public boolean isFirstReportEmpty() {
        return TextUtils.isEmpty(getString(Key.FIRST_REPORT));
    }
    public String getFirstReportDate() {
        return isFirstReportEmpty() ? "" : getString(Key.FIRST_REPORT);
    }

    public String getLatitude() {
        return getString(Key.LOC_LATITUDE);
    }
    public String getLongitude() {
        return getString(Key.LOC_LONGITUDE);
    }
    public String getAltitude() {
        return getString(Key.LOC_ALTITUDE);
    }
    public String getLocAccuracy() {
        return getString(Key.LOC_ACCURACY);
    }
    public String getSpeed() {
        return getString(Key.LOC_SPEED);
    }
    public String getBearing() {
        return getString(Key.LOC_BEARING);
    }
    public Boolean getHasAccuracy() {
        return getBoolean(Key.LOC_HAS_ACCURACY);
    }
    public Boolean getHasAltitude() {
        return getBoolean(Key.LOC_HAS_ALTITUDE);
    }
    public Boolean getHasSpeed() {
        return getBoolean(Key.LOC_HAS_SPEED);
    }
    public Boolean getHasBearing() {
        return getBoolean(Key.LOC_HAS_BEARING);
    }
    public Boolean getMockProvider() { return getBoolean(Key.LOC_MOCK_PROVIDER); }
    public String getProvider() {
        return getString(Key.LOC_PROVIDER);
    }
    public String getElapsedTime() {
        return getString(Key.LOC_ELAPSED_TIME);
    }

    public String getUniqueID() {
        return getString(Key.UNIQUE_ID);
    }

    public Boolean getHandlerCalledStatus() {
        return getBoolean(Key.IS_HANDLER_CALLED);
    }

    public Boolean getReportSentStatus() {
        return getBoolean(Key.IS_REPORT_SENT);
    }

    public String getMsgText() {
        return getString(Key.MSG_TEXT);
    }

    public boolean isMsgTextEmpty() {
        return TextUtils.isEmpty(getString(Key.MSG_TEXT));
    }

    public boolean isMsgTitleEmpty() {
        return TextUtils.isEmpty(getString(Key.MSG_TITLE));
    }

    public String getMsgTitle() {
        return getString(Key.MSG_TITLE);
    }

    public String getScreenOffTime() {
        return getString(Key.SCREEN_OFF_TIME);
    }

    public boolean isScreenOffEmpty() {
        return TextUtils.isEmpty(getString(Key.SCREEN_OFF_TIME));
    }

    public String getAlarmSentTime() {
        return getString(Key.ALARM_SENDING);
    }

    public boolean isAlarmSentEmpty() {
        return TextUtils.isEmpty(getString(Key.ALARM_SENDING));
    }



}