package org.gundartsev.edu.sensors.alert;

import org.gundartsev.edu.sensors.domain.alert.Alert;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Provide alerts for a sensor.
 */
public interface ISensorAlertService {
    /**
     * Get all alerts using non-blocking NoSQL drivers
     * @param sensorUUID Sensor's UUID
     * @return All alerts {@link Alert} for the sensor
     */
    Flux<Alert> getAlerts(UUID sensorUUID);
}
