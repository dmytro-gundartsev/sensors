package org.gundartsev.edu.sensors.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Value;

@Data
public class StatisticRing {
    private static int ABSENT_MARKER_MAX = 0; // currently consider absence of value as "0" measurement
    private static float ABSENT_MARKER_AVG = 0.0f; // currently consider absence of value as having "0.0" measurement
    private float[] avgLevels;
    private int[] maxLevels;
    private int size;
    private int latestPeriodId = 0;

    @Getter
    public static class StatisticValue {
        private int maxValue = 0;
        private float avgValue = 0.0f;
    }

    public StatisticRing(int size) {
        this.size = size;
        avgLevels = new float[size];
        maxLevels = new int[size];
    }

    public void put(int periodId, StatisticValue value) {
        if (latestPeriodId + 1 == periodId) {
            // statistic coming with no gaps. Most probable scenario
            putData(periodId, value);
        } else if (periodId - latestPeriodId < size) {
            // there was some gap to be filled with absent markers
            fillGaps(periodId);
            putData(periodId, value);
        } else {
            // new statistic came too late so new empty frame to be created
            clearFrame();
            putData(periodId, value);
        }
        latestPeriodId = periodId;
    }

    private void clearFrame() {
        avgLevels = new float[size];
        maxLevels = new int[size];
    }

    private void putData(int periodId, StatisticValue value) {
        int normalizedIdx = periodId % size;
        avgLevels[normalizedIdx] = value.getAvgValue();
        maxLevels[normalizedIdx] = value.getMaxValue();
    }

    private void fillGaps(int periodId) {
        for (int gapIdx = latestPeriodId + 1; gapIdx < periodId; gapIdx++) {
            avgLevels[gapIdx] = ABSENT_MARKER_AVG;
            avgLevels[gapIdx] = ABSENT_MARKER_AVG;
        }
    }

    public StatisticValue getStatistic(int currentPeriodId) {
        StatisticValue value = new StatisticValue();
        int boundaryPeriodId = currentPeriodId - size;
        int statCnt = latestPeriodId - boundaryPeriodId + 1;
        float accAvg = 0.0f;
        if (boundaryPeriodId >= latestPeriodId) {
            for (int idx = boundaryPeriodId; idx <= latestPeriodId; idx++) {
                value.maxValue = Math.max(value.maxValue, this.maxLevels[idx]);
                accAvg += avgLevels[idx];
            }
        }
        value.avgValue = accAvg / statCnt;
        return value;
    }
}
