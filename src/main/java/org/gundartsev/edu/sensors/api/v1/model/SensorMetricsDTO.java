package org.gundartsev.edu.sensors.api.v1.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SensorMetricsDTO {
    private int maxLast30Days;
    private int avgLast30Days;
}
