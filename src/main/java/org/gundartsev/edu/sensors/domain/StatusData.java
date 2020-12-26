package org.gundartsev.edu.sensors.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
public class StatusData implements Serializable {
    StatusEnum status = StatusEnum.OK;
    OffsetDateTime latestMeasurementTime;
    int latestUTCMinuteId = 0;
    byte statusCounter = 0;
}
