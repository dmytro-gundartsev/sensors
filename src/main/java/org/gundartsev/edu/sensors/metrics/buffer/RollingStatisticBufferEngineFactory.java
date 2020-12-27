package org.gundartsev.edu.sensors.metrics.buffer;

import org.gundartsev.edu.sensors.domain.HourStatisticData;
import org.springframework.stereotype.Service;

@Service
public class RollingStatisticBufferEngineFactory implements IRollingStatisticBufferEngineFactory {
    @Override
    public <T extends StatisticBufferData> IRollingStatisticBufferEngine<T> getEngine(BufferedTimeHorizon timeHorizon, T buffer) {
        if (timeHorizon == BufferedTimeHorizon.HOUR){
            return (IRollingStatisticBufferEngine<T>)(new RollingHourStatisticBufferEngine((HourStatisticData) buffer));
        } else {
            throw new IllegalStateException("Buffer is not implemented for non HOUR horizon");
        }
    }
}
