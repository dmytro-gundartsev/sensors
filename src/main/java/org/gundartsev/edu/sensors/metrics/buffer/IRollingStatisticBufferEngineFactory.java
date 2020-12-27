package org.gundartsev.edu.sensors.metrics.buffer;

public interface IRollingStatisticBufferEngineFactory {
    enum BufferedTimeHorizon {
        HOUR, DAY
    }
    <T extends StatisticBufferData> IRollingStatisticBufferEngine<T> getEngine(BufferedTimeHorizon timeHorizon, T buffer);
}
