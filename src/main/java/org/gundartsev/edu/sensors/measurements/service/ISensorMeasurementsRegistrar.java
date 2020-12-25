package org.gundartsev.edu.sensors.measurements.service;

import org.gundartsev.edu.sensors.domain.MeasurementData;

import java.util.UUID;

public interface ISensorMeasurementsRegistrar {
    void registerMeasurement(UUID uuid, MeasurementData data);
}
