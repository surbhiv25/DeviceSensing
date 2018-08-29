package com.ezeia.devicesensing.utils.Sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.ezeia.devicesensing.utils.Sensor.SensorModel.AbstractSensorModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class SensorController {

    private static final int TRANSFER_SERVICE_PERIOD = 5000;
    private final SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);


    private final Map<SensorType, AbstractSensorModel> availableSensors = new HashMap<>();

    private final SensorManager mSensorManager;
    private final Context mContext;

    public SensorController(SensorManager mSensorManager, Context mContext) {
        this.mSensorManager = mSensorManager;
        this.mContext = mContext;
    }

    /**
     * Initialize the system by checking the SensorManager for the presence on the
     * device of all sensors described in the SensorType enum. If present, add available sensors
     * to the member map availableSensors. Then schedule the corresponding transfer services.
     * */
    public void setup() {
        for (SensorType type : SensorType.values()) {
            if(mSensorManager.getDefaultSensor(type.getSensorIdentifier()) != null) {
                availableSensors.put(type, AbstractSensorModel.getSensorModelByType(type,mContext));
            }
        }
    }

    /**
     * When a new sensor event is triggered, check if corresponding sensor is registered in availableSensors. Then, check if sensor
     * is active. If yes, update the sensor's model by adding new sensor data to data queue. If the queue is returned,
     * write queue data to disk.
     * */
    public LinkedHashMap<String,String> onSensorEvent(SensorEvent event)
    {
        LinkedHashMap<String,String> buffer = new LinkedHashMap<>();
        int eventSensorIdentifier = event.sensor.getType();
        SensorType sensorTypeById = SensorType.getSensorTypeById(eventSensorIdentifier);
        if(availableSensors.containsKey(sensorTypeById)) {
            AbstractSensorModel sensorModel = availableSensors.get(sensorTypeById);
            if (sensorModel.isActive()) {

                long time = System.currentTimeMillis();
                String currentDateTime = mFormat.format(new Date(time));

                buffer = sensorModel.updateAndGetCurrentDataQueue(event, currentDateTime);

                /*String xAcc = Float.toString(event.values[0]);
                String yAcc = Float.toString(event.values[1]);
                String zAcc = Float.toString(event.values[2]);
                String timestamp = currentDateTime;

                buffer.append("X: "+xAcc+"\nY: "+yAcc+"\nZ: "+zAcc+"\nACCURACY: "+event.accuracy+
                        "\nTIMESTAMP: "+event.timestamp);*/
            }
        }

        return buffer;
    }

    /**
     * For all available sensors, register the corresponding sensor listener.
    * */
    public void registerListener(SensorEventListener listener, Sensor sensor)
    {
       /* for (SensorType sensorType : availableSensors.keySet())
        {
            Sensor sensor = mSensorManager.getDefaultSensor(sensorType.getSensorIdentifier());
            mSensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }*/

        mSensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterListener(SensorEventListener listener) {
        mSensorManager.unregisterListener(listener);
    }
}
