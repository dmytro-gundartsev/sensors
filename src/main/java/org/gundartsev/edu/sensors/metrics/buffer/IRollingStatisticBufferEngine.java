package org.gundartsev.edu.sensors.metrics.buffer;

import org.gundartsev.edu.sensors.domain.metrics.StatisticRing;
import org.gundartsev.edu.sensors.domain.metrics.StatisticSnapshot;
import org.gundartsev.edu.sensors.domain.metrics.StatisticValue;

public interface IRollingStatisticBufferEngine<T> {
    StatisticSnapshot accept(int periodUTCId, int co2Level);
    T getBuffer();
    StatisticValue calcBufferedStatisticFor(int periodId);
}
