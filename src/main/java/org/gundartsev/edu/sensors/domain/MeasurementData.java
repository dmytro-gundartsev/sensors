package org.gundartsev.edu.sensors.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class MeasurementData implements Serializable {
    private UUID uuid;
    private int level;
    private OffsetDateTime time;
}
