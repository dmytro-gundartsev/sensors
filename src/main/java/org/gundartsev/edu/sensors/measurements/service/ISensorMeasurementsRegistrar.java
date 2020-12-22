package org.gundartsev.edu.sensors.measurements.service;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ISensorMeasurementsRegistrar {
    void registerMeasurement(UUID uuid, OffsetDateTime time, int value);
}
