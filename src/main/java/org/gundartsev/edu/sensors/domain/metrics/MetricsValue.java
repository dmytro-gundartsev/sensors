package org.gundartsev.edu.sensors.domain.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * And entity containing metrics values (max and average value)
 */

@Data
@AllArgsConstructor
public class MetricsValue implements Serializable {
    private int maxValue;
    private float avgValue;
}
