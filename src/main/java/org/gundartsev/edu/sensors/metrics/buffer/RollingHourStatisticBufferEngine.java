package org.gundartsev.edu.sensors.metrics.buffer;

import lombok.Getter;
import org.gundartsev.edu.sensors.domain.HourStatisticData;
import org.gundartsev.edu.sensors.domain.metrics.StatisticSnapshot;

/**
 * Engine for calculating of statistic within hour using incremental approach
 * <br/> Main task of the engine is increase stat granularity from per minute to per hour for further
 * not that intense calculation.
 */
class RollingHourStatisticBufferEngine implements IRollingStatisticBufferEngine<HourStatisticData> {
    private static final int MINUTES_IN_HOUR = 60;
    @Getter
    private HourStatisticData buffer;

    RollingHourStatisticBufferEngine(HourStatisticData buffer) {
        this.buffer = buffer;
    }

    /**
     * Accept new measure from sensor and use it for calculation of incremental statistic artifacts
     *
     * @param minuteUTCId Minute ID from Epoch in UTC Zone
     * @param co2Level    Measurement value of CO2
     * @return If the measurement arrives for "new hour", so statistic artifacts to be adjusted and
     * {@link StatisticSnapshot created out of it}. Null - if current hour is not "completed" yet.
     */
    @Override
    public StatisticSnapshot accept(int minuteUTCId, int co2Level) {
        StatisticSnapshot snapshot = null;
        int hourUTCId = minuteUTCId / MINUTES_IN_HOUR; // int dividing truncates
        byte currentMinuteId = (byte) (minuteUTCId % MINUTES_IN_HOUR);
        // init phase
        if (buffer.getHourUTCId() == 0) {
            buffer.setHourUTCId(hourUTCId);
            buffer.setLastValue(co2Level);
        }
        // arrived data is for next hour. Finalization of open buffer and create a new one
        if (hourUTCId > buffer.getHourUTCId()) {
            HourStatisticData finalBuffer = finalizeBuffer();
            buffer = new HourStatisticData(hourUTCId, finalBuffer.getLastValue());
            snapshot = extractSnapshot(finalBuffer);
        }
        // arrived data is for a buffered hour or for a new hour which buffer is already created
        if (hourUTCId == buffer.getHourUTCId() && currentMinuteId >= buffer.getLastMinuteInHour()) {
            byte interval = (byte) (currentMinuteId - buffer.getLastMinuteInHour());
            accumulateStatistic(co2Level, interval);
            buffer.setLastMinuteInHour(currentMinuteId);
            buffer.setLastValue(co2Level);
        }
        // case of old data arrival. hourUTCId or minuteUTCId are less than those already in buffer
        return snapshot;
    }

    /**
     * Produces @{@link StatisticSnapshot} as if it was already finished. To be used for metrics values calculation
     *
     * @return Adjusted values of incremental alg. artifacts into {@link StatisticSnapshot}. Null - if no hour statistic
     * present yet.
     */
    public StatisticSnapshot currentBufferSnapshot() {
        if (buffer.getHourUTCId() == 0) {
            return null;
        }
        // getting current buffer statistic adjusted to the hour bounday
        int remainingInterval = MINUTES_IN_HOUR - (buffer.getLastMinuteInHour() % MINUTES_IN_HOUR);
        float accAvg = buffer.getAccAvgTimeWeighted() + buffer.getLastValue() * (float) remainingInterval / MINUTES_IN_HOUR;
        return StatisticSnapshot.builder().maxLevel(buffer.getMaxLevel()).avgLevel(accAvg).periodId(buffer.getHourUTCId()).build();
    }

    private HourStatisticData finalizeBuffer() {
        int remainingInterval = MINUTES_IN_HOUR - (buffer.getLastMinuteInHour() % MINUTES_IN_HOUR);
        accumulateStatistic(buffer.getLastValue(), (byte) remainingInterval);
        return buffer;
    }

    private StatisticSnapshot extractSnapshot(HourStatisticData finalBuffer) {
        return StatisticSnapshot.builder()
                .periodId(finalBuffer.getHourUTCId())
                .avgLevel(finalBuffer.getAccAvgTimeWeighted())
                .maxLevel(finalBuffer.getMaxLevel()).build();
    }

    private void accumulateStatistic(int co2Level, byte interval) {
        buffer.setMaxLevel(Math.max(buffer.getMaxLevel(), co2Level));
        buffer.setAccAvgTimeWeighted(buffer.getAccAvgTimeWeighted() + buffer.getLastValue() * (float) interval / MINUTES_IN_HOUR);
    }
}

