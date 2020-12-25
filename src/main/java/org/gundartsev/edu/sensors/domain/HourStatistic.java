package org.gundartsev.edu.sensors.domain;

import java.io.Serializable;

public class HourStatistic implements Serializable {
    int measurementsCnt = 0;    // defensive reserve for measurements count in case >> 60 per hour
    float accAvgLevel = 0.0f;
    int maxLevel = 0;

    public void put(int level) {
        measurementsCnt++;
        maxLevel = Math.max(level, maxLevel);
        accAvgLevel += (level - accAvgLevel) / measurementsCnt; // == (accAvgLevel * (measurementsCnt - 1) + level) / measurementsCnt
    }
}
