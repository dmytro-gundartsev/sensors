package org.gundartsev.edu.sensors.domain.metrics;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Buffer of accumulating hourly {@link StatisticSnapshot} and providing logic for adding snapshots in the buffer and
 * calculate statistic based on the buffer content.
 * <br/>General concept:
 * <br/>- Hourly stastic may come sparse (so sensor might not send measurements for some time, so there might be "gaps"
 * between last accepted and a new coming snapshot, so the target structure - to fix the snapshots as some kind of events
 * and then build statistic calculation logic on top of them.
 * <br/>- statistic calculation is a time weighted one. In other words if there is a gap between two snapshots, then we
 * assume that there the gap is to be filled with values (i.e. the same statistic) of previous snapshot.
 * <br/>- all hourly computed {@link StatisticSnapshot} are accumulated in the sequenced collection
 * (latest - first, oldest-last). For memory efficiency this collection is stored as arrays (least memory consuming
 * data structure).
 * <br/>- when new snapshot arrives we put it into collection as first element and trim collection
 * (removing trailing elements) in case we have values, which are too old to be used in calculation of last 30d statistic
 * <br/>- statistic calculation is based on iterating through the collection of snapshots and recalculate avg and max.
 * (Time weight aware algorithm is used)
 */
@Data
public class MetricsBuffer implements Serializable {
    public static final int MAX_BUFFFER_SIZE = 43200; // equals to 30 days of keeping statistic snapshots per minute
    // last received elements are stored in 0=index.
    private float[] avgLevels;
    private int[] maxLevels;
    private int[] periodIds;
    private final int maxPeriodSize;    // number of time periods (current time period for snapshot - hour, so - number
                                        // max number of hours for which we do statistic
    private int lastPeriodId() {
        return periodIds.length == 0 ? 0 : periodIds[0];
    }

    private LinkedList<StatisticSnapshot> asList() {
        LinkedList<StatisticSnapshot> statisticSnapshots = new LinkedList<>();
        for (int idx = 0; idx < periodIds.length; idx++) {
            statisticSnapshots.add(StatisticSnapshot.builder()
                    .periodId(periodIds[idx])
                    .avgLevel(avgLevels[idx])
                    .maxLevel(maxLevels[idx]).build());
        }
        return statisticSnapshots;
    }

    /**
     * Add {@link StatisticSnapshot} to the buffer and do buffer trimming if needed.
     * <br/>Buffer trimming is for two purposes: to throw away too old snapshots which cannot be used for [rotationSize]
     * hour statistic (eq. to 30 last days) and to keep the size of the buffer not exceeding of [rotationSize] size.
     *
     * @param snapshot Statistic snapshot to be added to the buffer
     */
    public void put(StatisticSnapshot snapshot) {
        LinkedList<StatisticSnapshot> list = asList();
        // normally snapshots go in sequence with periodId(hourUTCId) rising
        if (list.isEmpty() || list.getFirst().getPeriodId() < snapshot.getPeriodId()) {
            list.addFirst(snapshot);
        } else {
            // if sequence is broken - find proper place for inserting snapshot
            int idx = 0;
            Iterator<StatisticSnapshot> iterator = list.iterator();
            while (iterator.hasNext() && iterator.next().getPeriodId() > snapshot.getPeriodId()) {
                idx++;
            }
            list.add(idx, snapshot);
        }
        trimIfNeeded(list);
        persistIntoArrays(list);
    }

    private void persistIntoArrays(LinkedList<StatisticSnapshot> list) {
        avgLevels = new float[list.size()];
        maxLevels = new int[list.size()];
        periodIds = new int[list.size()];
        Iterator<StatisticSnapshot> iterator = list.iterator();
        int idx = 0;
        while (iterator.hasNext()) {
            StatisticSnapshot sn = iterator.next();
            avgLevels[idx] = sn.getAvgLevel();
            maxLevels[idx] = sn.getMaxLevel();
            periodIds[idx] = sn.getPeriodId();
            idx++;
        }
    }

    private void trimIfNeeded(LinkedList<StatisticSnapshot> list) {
        int lastPeriodId = list.getFirst().periodId;
        int boundaryPeriodId = lastPeriodId - (maxPeriodSize - 1);
        StatisticSnapshot lastSnapshot;
        do {
            lastSnapshot = list.removeLast();
        } while (lastSnapshot.getPeriodId() < boundaryPeriodId
                && !list.isEmpty()
                && list.getLast().getPeriodId() <= boundaryPeriodId);
        if (list.size() < maxPeriodSize) {
            list.addLast(lastSnapshot);
        }
    }

    public MetricsBuffer(int maxPeriodSize) {
        if (maxPeriodSize < 1 || maxPeriodSize > MAX_BUFFFER_SIZE) {
            throw new IllegalArgumentException("Invalid buffer size (maxPeriodSize) must be greater than zero" +
                    "and not exceed [" + MAX_BUFFFER_SIZE + "]");
        }
        this.maxPeriodSize = maxPeriodSize;
        avgLevels = new float[0];
        maxLevels = new int[0];
        periodIds = new int[0];
    }

    /**
     * Calculate statistic based on current buffer + augmented part of StatisticSnapshot which is not yet finished in
     * hour buffer but must participate in the assesment of the metrics.
     *
     * @param augmentedCurrentBuffer {@link StatisticSnapshot} instance made by HourBuffer from currently buffered
     *                               (unfinished buffer) values to be also considered in the calculation.
     *                               Can be null if hour buffer is empty now.
     * @param reportPeriodId         Current hourID (hours from Epoch) to be taken as a boundary for range calculation
     * @return Calculated time-weighted metric values
     * @throws IllegalArgumentException Happens when attempted NOW moment (reportPeriodId) lies in the "past" of
     *                                  registered snapshots so statistic cannot be produced properly
     */
    public MetricsValue calculate(@Nullable StatisticSnapshot augmentedCurrentBuffer, int reportPeriodId) throws IllegalArgumentException {
        if (reportPeriodId < lastPeriodId()) {
            throw new IllegalArgumentException("Reporting moment cannot be in the past comparing to the last received snapshot");
        }
        LinkedList<StatisticSnapshot> snapshots = asList();
        if (augmentedCurrentBuffer != null) {
            snapshots.addFirst(augmentedCurrentBuffer);
        }
        int currentPeriodId = reportPeriodId + 1; // will count as current hour (reportPeriodId) is already finished
        int boundaryPeriodId = currentPeriodId - maxPeriodSize;
        int totalPeriodsCnt = 0;
        boolean quit = (boundaryPeriodId > lastPeriodId());
        int maxVal = 0;
        float accAvg = 0.0f;
        Iterator<StatisticSnapshot> iterator = snapshots.iterator();
        while (iterator.hasNext() && !quit) {
            StatisticSnapshot curSnapshot = iterator.next();
            int valuePeriodsCnt = currentPeriodId - Math.max(curSnapshot.getPeriodId(), boundaryPeriodId);
            totalPeriodsCnt += valuePeriodsCnt;
            maxVal = Math.max(maxVal, curSnapshot.maxLevel);
            accAvg += curSnapshot.avgLevel * valuePeriodsCnt;
            quit = curSnapshot.getPeriodId() <= boundaryPeriodId;
            currentPeriodId = curSnapshot.getPeriodId();
        }
        accAvg /= Math.max(totalPeriodsCnt, 1);
        return new MetricsValue(maxVal, accAvg);
    }
}
