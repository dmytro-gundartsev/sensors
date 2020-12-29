package org.gundartsev.edu.sensors.metrics.buffer;

import org.gundartsev.edu.sensors.domain.metrics.StatisticSnapshot;
import org.springframework.lang.Nullable;

public interface IRollingStatisticBufferEngine<T> {
    StatisticSnapshot accept(int periodUTCId, int co2Level);
    T getBuffer();
    @Nullable
    StatisticSnapshot currentBufferSnapshot(int periodId);
}
