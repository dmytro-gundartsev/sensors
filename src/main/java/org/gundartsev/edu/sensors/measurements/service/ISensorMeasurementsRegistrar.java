package org.gundartsev.edu.sensors.measurements.service;

import org.gundartsev.edu.sensors.domain.SensorData;

import java.util.UUID;

public interface ISensorMeasurementsRegistrar {
    void registerMeasurement(UUID uuid, SensorData data);
}
