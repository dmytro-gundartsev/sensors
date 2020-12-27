package org.gundartsev.edu.sensors.domain;

import lombok.Data;
import org.gundartsev.edu.sensors.metrics.buffer.StatisticBufferData;

import java.io.Serializable;

@Data
public class HourStatisticData implements Serializable, StatisticBufferData {
    int hourUTCId = 0;
    byte lastMinuteId = 0;
    int lastValue = 0;
    float accAvgLevel = 0.0f;

    public HourStatisticData(int hourUTCId, int lastValue) {
        this.hourUTCId = hourUTCId;
        this.lastValue = lastValue;
    }

    public HourStatisticData() {
    }

    int maxLevel = 0;
}
