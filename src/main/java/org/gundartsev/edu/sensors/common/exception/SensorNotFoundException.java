package org.gundartsev.edu.sensors.common.exception;

import java.util.UUID;

/**
 * Exception for indicating of requests of the data for non-present sensor
 */
public class SensorNotFoundException extends RuntimeException {
    private final UUID sensorUUID;

    public SensorNotFoundException(UUID sensorUUID) {
        super("Data for sensor UUID [" + sensorUUID + "] not found");
        this.sensorUUID = sensorUUID;
    }
}
