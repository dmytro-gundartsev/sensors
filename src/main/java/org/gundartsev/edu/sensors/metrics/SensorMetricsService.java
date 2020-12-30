package org.gundartsev.edu.sensors.metrics;

import com.hazelcast.map.IMap;
import org.gundartsev.edu.sensors.common.exception.SensorNotFoundException;
import org.gundartsev.edu.sensors.common.mq.fetchers.IMessageConsumingService;
import org.gundartsev.edu.sensors.domain.HourStatisticData;
import org.gundartsev.edu.sensors.domain.SensorData;
import org.gundartsev.edu.sensors.domain.metrics.MetricsBuffer;
import org.gundartsev.edu.sensors.domain.metrics.MetricsValue;
import org.gundartsev.edu.sensors.domain.metrics.StatisticSnapshot;
import org.gundartsev.edu.sensors.metrics.buffer.IRollingStatisticBufferEngine;
import org.gundartsev.edu.sensors.metrics.buffer.IRollingStatisticBufferEngineFactory;
import org.gundartsev.edu.sensors.metrics.buffer.RollingStatisticBufferEngineFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Service for processing incoming {@link StatisticSnapshot} messages into {@link MetricsBuffer}.
 * also provides calculated metrics for last 30 DAYS
 */
@Service
public class SensorMetricsService implements IMessageConsumingService<StatisticSnapshot>, ISensorMetricsService {
    public static final int STATISTIC_METRIC_BUFFER_SIZE = 30 * 24; // Buffer will contain data for 30 days
    private static final int SECONDS_IN_HOUR = 60 * 60;
    private final IMap<UUID, MetricsBuffer> metricBufferMap;
    private final IMap<UUID, SensorData> sensorDataMap;
    private final RollingStatisticBufferEngineFactory rollingStatisticBufferEngineFactory;

    public SensorMetricsService(IMap<UUID, MetricsBuffer> metricBufferMap,
                                IMap<UUID, SensorData> sensorDataMap,
                                RollingStatisticBufferEngineFactory rollingStatisticBufferEngineFactory) {
        this.metricBufferMap = metricBufferMap;
        this.sensorDataMap = sensorDataMap;
        this.rollingStatisticBufferEngineFactory = rollingStatisticBufferEngineFactory;
    }

    /**
     * Process the incoming StatisticSnapshot message by putting it into buffer
     * @param message {@link StatisticSnapshot} message instance to be processed
     */
    @Override
    public void apply(StatisticSnapshot message) {
        MetricsBuffer buffer = metricBufferMap.getOrDefault(message.getSensorUUID(), new MetricsBuffer(STATISTIC_METRIC_BUFFER_SIZE));
        buffer.put(message);
        metricBufferMap.set(message.getSensorUUID(), buffer);
    }

    public MetricsValue getMetrics(UUID sensorUUID) throws SensorNotFoundException {
        int currentHourId = currentUTCHourId();
        StatisticSnapshot currentBufferedValues = null;
        if (sensorDataMap.containsKey(sensorUUID)) {
            SensorData sensorData = sensorDataMap.get(sensorUUID);
            IRollingStatisticBufferEngine<HourStatisticData> engine = rollingStatisticBufferEngineFactory.getEngine(IRollingStatisticBufferEngineFactory.BufferedTimeHorizon.HOUR, sensorData.getRollingHourStatistic());
            currentBufferedValues = engine.currentBufferSnapshot();
        }
        if (metricBufferMap.containsKey(sensorUUID)) {
            MetricsBuffer metricsBuffer = metricBufferMap.get(sensorUUID);
            return metricsBuffer.calculate(currentBufferedValues, currentHourId);
        }
        if (currentBufferedValues != null) {
            return new MetricsValue(currentBufferedValues.getMaxLevel(), currentBufferedValues.getAvgLevel());
        } else {
            throw new SensorNotFoundException(sensorUUID);
        }
    }

    private int currentUTCHourId() {
        // getting current minute number from Epoch, int value is safe for the next centuries.
        return (int) (OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toEpochSecond() / SECONDS_IN_HOUR);
    }
}
