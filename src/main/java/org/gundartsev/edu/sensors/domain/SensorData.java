package org.gundartsev.edu.sensors.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class SensorData implements Serializable {
    StatusData statusData = new StatusData();
    HourStatistic rollingHourStatistic = new HourStatistic();
}
