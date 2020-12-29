package org.gundartsev.edu.sensors.metrics.buffer;

import org.gundartsev.edu.sensors.domain.HourStatisticData;
import org.gundartsev.edu.sensors.domain.metrics.StatisticSnapshot;

class RollingHourStatisticBufferEngine implements IRollingStatisticBufferEngine<HourStatisticData> {
    private static final int MINUTES_IN_HOUR = 60;
    private HourStatisticData buffer;

    RollingHourStatisticBufferEngine(HourStatisticData buffer) {
        this.buffer = buffer;
    }

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
        if (hourUTCId == buffer.getHourUTCId() && currentMinuteId >= buffer.getLastMinuteId()) {
            byte interval = (byte) (currentMinuteId - buffer.getLastMinuteId());
            accumulateStatistic(co2Level, interval);
            buffer.setLastMinuteId(currentMinuteId);
            buffer.setLastValue(co2Level);
        }
        // case of old data arrival. hourUTCId or minuteUTCId are less than those already in buffer
        return snapshot;
    }

    public StatisticSnapshot currentBufferSnapshot(int periodId) {
        if (buffer.getHourUTCId() == 0 || periodId < buffer.getLastMinuteId()) {
            return null;
        }
        // getting current buffer statistic adjusted to the hour bounday
        int remainingInterval = MINUTES_IN_HOUR - (buffer.getLastMinuteId() % MINUTES_IN_HOUR);
        float accAvg = buffer.getAccAvgLevel() + buffer.getLastValue() * (float) remainingInterval / MINUTES_IN_HOUR;
        return StatisticSnapshot.builder().maxLevel(buffer.getMaxLevel()).avgLevel(accAvg).periodId(buffer.getHourUTCId()).build();
    }

    private HourStatisticData finalizeBuffer() {
        int remainingInterval = MINUTES_IN_HOUR - (buffer.getLastMinuteId() % MINUTES_IN_HOUR);
        accumulateStatistic(buffer.getLastValue(), (byte) remainingInterval);
        return buffer;
    }

    private StatisticSnapshot extractSnapshot(HourStatisticData finalBuffer) {
        return StatisticSnapshot.builder()
                .periodId(finalBuffer.getHourUTCId())
                .avgLevel(finalBuffer.getAccAvgLevel())
                .maxLevel(finalBuffer.getMaxLevel()).build();
    }

    private void accumulateStatistic(int co2Level, byte interval) {
        buffer.setMaxLevel(Math.max(buffer.getMaxLevel(), co2Level));
        buffer.setAccAvgLevel(buffer.getAccAvgLevel() + buffer.getLastValue() * (float) interval / MINUTES_IN_HOUR);
    }


    @Override
    public HourStatisticData getBuffer() {
        return buffer;
    }
}
