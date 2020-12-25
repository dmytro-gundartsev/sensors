package org.gundartsev.edu.sensors.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
public class SensorData implements Serializable {
    OffsetDateTime latestMeasurement;
    int latestPeriodId = 0;
    StatusData status;
    HourStatistic rollingHourStatistic;
}
