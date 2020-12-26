package org.gundartsev.edu.sensors.domain.metrics;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class StatisticSnapshot implements Serializable {
    int hourUTCId;
    float avgLevel;
    int maxLevel;
}
