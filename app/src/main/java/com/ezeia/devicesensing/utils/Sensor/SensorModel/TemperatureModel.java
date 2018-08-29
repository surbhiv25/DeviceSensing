package com.ezeia.devicesensing.utils.Sensor.SensorModel;

import android.hardware.SensorEvent;

import java.util.LinkedHashMap;


/**
 *  TempratureModel.java
 *  AndroidSensorApp
 *
 *  © 2016 Zühlke Engineering AG. All rights reserved.
 */
public class TemperatureModel extends AbstractSensorModel {

    private static TemperatureModel instance = null;

    private TemperatureModel() {
    }

    public static synchronized TemperatureModel getInstance() {
        if (instance == null){
            instance = new TemperatureModel();
        }
        return instance;
    }

    @Override
    protected LinkedHashMap<String,String> getLastDataEntry(SensorEvent event, String dateTime)
    {
        LinkedHashMap<String,String> hashMap = new LinkedHashMap<>();
        String temperatureInCelsius = Float.toString(event.values[0]);

        hashMap.put("TEMPERATURE: ",temperatureInCelsius);
        hashMap.put("TIMESTAMP: ", String.valueOf(dateTime));

        return hashMap;
    }

}