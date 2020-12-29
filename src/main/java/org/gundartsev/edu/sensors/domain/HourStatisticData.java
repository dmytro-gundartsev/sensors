package org.gundartsev.edu.sensors.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.gundartsev.edu.sensors.metrics.buffer.StatisticBufferData;

import java.io.Serializable;

/**
 * Entity for gathering and keeping the data for statistic incremental calculation within the hour
 */
@Data
@AllArgsConstructor
public class HourStatisticData implements Serializable, StatisticBufferData {
    int hourUTCId = 0; // hour UTC id from Epoch for which statistic is to be collected
    byte lastMinuteInHour = 0; // minute in hour of last received value
    int lastValue = 0; // last received value (needed for adjustments of avg)
    // hour statistic gathering variables
    float accAvgTimeWeighted = 0.0f; // accumulated time-weighted avg
    int maxLevel = 0; // max level within the hour


    public HourStatisticData(int hourUTCId, int lastValue) {
        this.hourUTCId = hourUTCId;
        this.lastValue = lastValue;
    }

    public HourStatisticData() {
    }
}
