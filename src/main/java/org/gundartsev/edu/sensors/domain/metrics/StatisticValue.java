package org.gundartsev.edu.sensors.domain.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class StatisticValue implements Serializable {
    private int maxValue;
    private float avgValue;
}
