package com.ezeia.devicesensing.utils.Sensor.SensorData;

import com.ezeia.devicesensing.utils.Sensor.SensorType;
import org.json.JSONException;
import org.json.JSONObject;

abstract class AbstractSensorData{

    private final SensorType sensorType;
    /**
     * The event timestamp stores the time in nanoseconds since the last device boot.
     * In case you need an absolute timestamp, consider calling System.currentTimeInMillis
     * when registering an event.
     */
    private final String timestamp;

    AbstractSensorData(SensorType sensorType, String timestamp) {
        this.sensorType = sensorType;
        this.timestamp = timestamp;
    }

    private SensorType getSensorType() {
        return sensorType;
    }

    /**
     * Generates a JSON Object out of a AbstractSensorData object. This method
     * is used by FileWriteService to write sensor data values into the file
     * system as JSON array.
     *
     * The format of the JSON Object can be shown using an accelerometer data point as an example:
     *
     * {
     *     "type": "Accelerometer",
     *     "date": "2016-09-013T12:08:56.235-0700",
     *     "x": "0.637745",
     *     "y": "-2.3455",
     *     "z": "0.6756464"
     * }
     */
    JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("type", getSensorType());
        object.put("date", timestamp);
        return object;
    }

}
