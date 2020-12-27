package org.gundartsev.edu.sensors.domain.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Value;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Arrays;

@Data
public class StatisticRing implements Serializable {
    private static int ABSENT_MARKER_MAX = -1; // currently consider absence of value as "0" measurement
    private static float ABSENT_MARKER_AVG = 0.0f; // currently consider absence of value as having "0.0" measurement
    private float[] avgLevels;
    private int[] maxLevels;
    private final int size;
    private int latestPeriodId = 0;
    @Nullable
    private StatisticValue latestValue;


    public StatisticRing(int size) {
        this.size = size;
        avgLevels = new float[size];
        maxLevels = new int[size];
        Arrays.fill(maxLevels, -1);
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
        Arrays.fill(avgLevels, 0.0f);
        Arrays.fill(maxLevels, -1);
    }

    private void putData(int periodId, StatisticValue value) {
        int normalizedIdx = periodId % size;
        avgLevels[normalizedIdx] = value.getAvgValue();
        maxLevels[normalizedIdx] = value.getMaxValue();
        latestValue = value;
    }

    private void fillGaps(int periodId) {
        for (int gapIdx = latestPeriodId + 1; gapIdx < periodId; gapIdx++) {
            int idx = gapIdx % size;
            avgLevels[idx] = ABSENT_MARKER_AVG;
            maxLevels[idx] = ABSENT_MARKER_MAX;
        }
    }

    public StatisticValue getStatistic(int reportPeriodId) throws IllegalArgumentException {
        if (reportPeriodId < latestPeriodId) {
            throw new IllegalArgumentException("Reporting moment cannot be in the past comparing to the last received snapshot");
        }
        int startPeriodId = reportPeriodId - (size - 1);
        float accAvg = 0.0f;
        int maxValue = 0;
        int gapSlotsCnt = 0;
        int totalSlots = 0;
        for (int periodId = reportPeriodId; periodId >= startPeriodId; periodId--) {
            int idx = periodId % size;
            gapSlotsCnt++;
            if (maxLevels[idx] > -1) {
                maxValue = Math.max(maxValue, maxLevels[idx]);
                accAvg = avgLevels[idx] * gapSlotsCnt;
                totalSlots += gapSlotsCnt;
                gapSlotsCnt = 0;
            }
        }
        accAvg /= totalSlots; // get time
        return new StatisticValue(maxValue, accAvg);
    }
}
