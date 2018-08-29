package com.ezeia.devicesensing.utils.Sensor.SensorData;

import com.ezeia.devicesensing.utils.Sensor.SensorType;
import org.json.JSONException;
import org.json.JSONObject;

class AccelerometerData extends AbstractSensorData {

    private final String xAcceleration;
    private final String yAcceleration;
    private final String zAcceleration;

    public AccelerometerData(String xAcceleration, String yAcceleration, String zAcceleration, String timestamp) {
        super(SensorType.ACCELEROMETER, timestamp);
        this.xAcceleration = xAcceleration;
        this.yAcceleration = yAcceleration;
        this.zAcceleration = zAcceleration;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = super.toJSON();
        object.put("x", this.xAcceleration);
        object.put("y", this.yAcceleration);
        object.put("z", this.zAcceleration);
        return object;
    }
}