package com.ezeia.devicesensing.utils;

import com.ezeia.devicesensing.SqliteRoom.Database.AppDatabase;
import com.ezeia.devicesensing.SqliteRoom.utils.DatabaseInitializer;

import java.util.ArrayList;

public class Constants
{
    public static final String TAG = "DATA ITEMS";

    public interface ACTION {
        String MAIN_ACTION = "action.main";
        String STARTFOREGROUND_ACTION = "action.startforeground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }

    public static String[] probeList = {"DeviceInfo","Install","Uninstall","AppUsage","Sensor","Bluetooth_State",
                                        "Bluetooth_Connection","WifiConnection","Airplane","Accounts","AudioFiles",
                                        "ImageFiles","VideoFiles","SMS","CallLogs","Contacts","Battery","RAM",
                                        "Internal","External","CellTower","Location","BatteryPlug"};

}
