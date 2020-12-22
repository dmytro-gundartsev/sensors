package org.gundartsev.edu.sensors.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;
@Data
@Builder
public class SensorData implements Serializable {
    private int level;
    private OffsetDateTime time;
}
