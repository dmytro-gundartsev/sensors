package org.gundartsev.edu.sensors.metrics;

import org.gundartsev.edu.sensors.common.exception.SensorNotFoundException;
import org.gundartsev.edu.sensors.domain.metrics.StatisticValue;

import java.util.UUID;

public interface ISensorMetricsService {
    StatisticValue getMetrics(UUID sensorUUID) throws SensorNotFoundException;
}
