package com.ezeia.devicesensing.utils.Sensor;

import android.hardware.Sensor;

public enum SensorType {

    ACCELEROMETER(Sensor.TYPE_ACCELEROMETER),
    TEMPERATURE(Sensor.TYPE_AMBIENT_TEMPERATURE);

    private final int sensorIdentifier;

    SensorType(int sensorIdentifier) {
        this.sensorIdentifier = sensorIdentifier;
    }

    public static SensorType getSensorTypeById(int sensorIdentifier) {
        for (SensorType sensorType : SensorType.values()) {
            if (sensorType.getSensorIdentifier() == sensorIdentifier) {
                return sensorType;
            }
        }
        throw new RuntimeException("SensorType not found for id " + sensorIdentifier);
    }

    public int getSensorIdentifier() {
        return sensorIdentifier;
    }

        public String toString() {
                return capitalize(this.name());
        }

        private String capitalize(final String line) {
                return Character.toUpperCase(line.charAt(0)) + line.substring(1).toLowerCase();
        }

}
