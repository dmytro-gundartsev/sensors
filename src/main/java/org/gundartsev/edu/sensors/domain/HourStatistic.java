package org.gundartsev.edu.sensors.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class HourStatistic implements Serializable {
    int hourUTCId = 0;
    byte lastMinuteId = 0;
    int lastValue = 0;
    float accAvgLevel = 0.0f;

    public HourStatistic(int hourUTCId, int lastValue) {
        this.hourUTCId = hourUTCId;
        this.lastValue = lastValue;
    }

    public HourStatistic() {
    }

    int maxLevel = 0;
}
