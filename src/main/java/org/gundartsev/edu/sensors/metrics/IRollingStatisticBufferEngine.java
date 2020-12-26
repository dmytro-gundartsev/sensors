package org.gundartsev.edu.sensors.metrics;

import org.gundartsev.edu.sensors.domain.metrics.StatisticSnapshot;

public interface IRollingStatisticBufferEngine<T> {
    StatisticSnapshot accept(int minuteUTCId, int co2Level);
    T getBuffer();
}
