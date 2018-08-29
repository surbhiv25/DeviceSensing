package com.ezeia.devicesensing.utils.Sensor.SensorData;

import com.ezeia.devicesensing.utils.Sensor.SensorType;
import org.json.JSONException;
import org.json.JSONObject;

class TemperatureData extends AbstractSensorData {

    private final String temperatureInCelsius;

    public TemperatureData(String temperatureInCelsius, String timestamp) {
        super(SensorType.TEMPERATURE, timestamp);
        this.temperatureInCelsius = temperatureInCelsius;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = super.toJSON();
        object.put("temperature", this.temperatureInCelsius);
        return object;
    }
}
