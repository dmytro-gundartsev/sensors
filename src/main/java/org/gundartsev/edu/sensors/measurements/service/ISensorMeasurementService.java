package org.gundartsev.edu.sensors.measurements.service;

import org.gundartsev.edu.sensors.domain.MeasurementData;

public interface ISensorMeasurementService {
    void apply(MeasurementData measurement);
}
