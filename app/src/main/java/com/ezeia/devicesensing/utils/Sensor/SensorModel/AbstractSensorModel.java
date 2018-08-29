package com.ezeia.devicesensing.utils.Sensor.SensorModel;

import android.content.Context;
import android.hardware.SensorEvent;
import com.ezeia.devicesensing.utils.Sensor.SensorType;

import java.util.LinkedHashMap;

public abstract class AbstractSensorModel {

    protected static final int CACHE_SIZE = 100;

    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    protected abstract LinkedHashMap<String,String> getLastDataEntry(SensorEvent event, String timestamp);

    public LinkedHashMap<String,String> updateAndGetCurrentDataQueue(SensorEvent event, String timestamp) {
        return getLastDataEntry(event, timestamp);
    }

    public static AbstractSensorModel getSensorModelByType(SensorType type, Context ctx) {
        switch (type) {
            case ACCELEROMETER:         return AccelerometerModel.getInstance(ctx);
            case TEMPERATURE:           return TemperatureModel.getInstance();
            default:                    return null;
        }
    }
}
