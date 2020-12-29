package org.gundartsev.edu.sensors.metrics;

import org.gundartsev.edu.sensors.common.exception.SensorNotFoundException;
import org.gundartsev.edu.sensors.domain.metrics.MetricsValue;

import java.util.UUID;

public interface ISensorMetricsService {
    MetricsValue getMetrics(UUID sensorUUID) throws SensorNotFoundException;
}
