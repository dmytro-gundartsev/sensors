package org.gundartsev.edu.sensors.domain.metrics;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.*;

/**
 * Buffer of accumulating hourly {@link StatisticSnapshot} and providing logic for adding snapshots in the buffer and
 * calculate statistic based on the buffer content.
 * <br/>General concept:
 * <br/>- all hourly computed {@link StatisticSnapshot} are accumulated in the sequenced collection
 * (latest - first, oldest-last). For memory efficiency this collection is stored as arrays (least memory consuming
 * data structure).
 * <br/>- when new snapshot arrives we put it into collection as first element and trim collection
 * (removing trailing elements) in case we have values, which are too old to be used in calculation of last 30d statistic
 * <br/>- Statistic calculation is based on iterating through the collection of snapshots and recalulate avg and max.
 * (Time weight aware algorithm is used)
 */
@Data
public class MetricBuffer implements Serializable {
    // last received elements are stored in 0=index.
    private float[] avgLevels;
    private int[] maxLevels;
    private int[] periodIds;
    private final int rotationSize;

    private int lastPeriodId(){
        return periodIds[0];
    }

    private LinkedList<StatisticSnapshot> asList(){
        LinkedList<StatisticSnapshot> statisticSnapshots = new LinkedList<>();
        for (int idx =0; idx < periodIds.length; idx++){
            statisticSnapshots.add(StatisticSnapshot.builder()
                    .hourUTCId(periodIds[idx])
                    .avgLevel(avgLevels[idx])
                    .maxLevel(maxLevels[idx]).build());
        }
        return statisticSnapshots;
    }

    public void putAndRotate(StatisticSnapshot snapshot){
        LinkedList<StatisticSnapshot> list = asList();
        list.addFirst(snapshot);
        trimIfNeeded(list);
        persistIntoArrays(list);
    }

    private void persistIntoArrays(LinkedList<StatisticSnapshot> list) {
        avgLevels = new float[list.size()];
        maxLevels = new int[list.size()];
        periodIds = new int[list.size()];
        Iterator<StatisticSnapshot> iterator = list.iterator();
        int idx = 0;
        while(iterator.hasNext()){
            StatisticSnapshot sn = iterator.next();
            avgLevels[idx] = sn.getAvgLevel();
            maxLevels[idx] = sn.getMaxLevel();
            periodIds[idx] = sn.getHourUTCId();
        }
    }

    private void trimIfNeeded(LinkedList<StatisticSnapshot> list) {
        int lastPeriodId = list.getFirst().hourUTCId;
        int boundaryPeriodId = lastPeriodId - (rotationSize - 1);
        StatisticSnapshot lastSnapshot;
        do {
            lastSnapshot = list.removeLast();
        } while (lastSnapshot.getHourUTCId() < boundaryPeriodId
                && !list.isEmpty()
                && list.getLast().getHourUTCId() <= boundaryPeriodId);
        if (list.size() < rotationSize){
            list.addLast(lastSnapshot);
        }
    }

    public MetricBuffer(int rotationSize) {
        this.rotationSize = rotationSize;
        avgLevels = new float [0];
        maxLevels = new int [0];
        periodIds = new int [0];
    }

    /**
     * Calculate statistic based on current buffer + augmented part of StatisticSnapshot which is not yet finished in
     * hour buffer but must participate in the assesment of the metrics.
     * @param augmentedCurrentBuffer {@link StatisticSnapshot} instance made by HourBuffer from currently buffered
     *                                                        (unfinished buffer) values to be also considered in the calculation.
     *                                                        Can be null if hour buffer is empty now.
     * @param reportPeriodId Current hourID (hours from Epoch) to be taken as a boundary for range calculation
     * @return Calculated time-weighted metric values
     * @throws IllegalArgumentException Happens when attempted NOW moment (reportPeriodId) lies in the "past" of
     * registered snapshots so statistic cannot be produced properly
     */
    public StatisticValue getStatistic(@Nullable StatisticSnapshot augmentedCurrentBuffer, int reportPeriodId) throws IllegalArgumentException {
        if (reportPeriodId < lastPeriodId()) {
            throw new IllegalArgumentException("Reporting moment cannot be in the past comparing to the last received snapshot");
        }
        LinkedList<StatisticSnapshot> snapshots = asList();
        if (augmentedCurrentBuffer != null) {
            snapshots.addFirst(augmentedCurrentBuffer);
        }
        int boundaryPeriodId = reportPeriodId - (rotationSize - 1);
        int currentPeriodId = reportPeriodId;
        int totalPeriodsCnt = 0;
        boolean quit = (boundaryPeriodId > lastPeriodId());
        int maxVal = 0;
        float accAvg = 0.0f;
        Iterator<StatisticSnapshot> iterator = snapshots.iterator();
        while (iterator.hasNext() && !quit) {
            StatisticSnapshot curSnapshot = iterator.next();
            int valuePeriodsCnt = currentPeriodId - Math.max(curSnapshot.getHourUTCId(), boundaryPeriodId);
            totalPeriodsCnt +=valuePeriodsCnt;
            maxVal = Math.max(maxVal, curSnapshot.maxLevel);
            accAvg += curSnapshot.avgLevel * valuePeriodsCnt;
            quit = curSnapshot.getHourUTCId() <= boundaryPeriodId;
            currentPeriodId = curSnapshot.getHourUTCId();
        }
        accAvg /= Math.max(totalPeriodsCnt, 1);
        return new StatisticValue(maxVal, accAvg);
    }
}
