package org.gundartsev.edu.sensors.metrics;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.gundartsev.edu.sensors.common.mq.fetchers.IMessageConsumingService;
import org.gundartsev.edu.sensors.config.CachingConfig;
import org.gundartsev.edu.sensors.domain.metrics.StatisticRing;
import org.gundartsev.edu.sensors.domain.metrics.StatisticSnapshot;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SensorMetricsService implements IMessageConsumingService<StatisticSnapshot> {
    public static final int  STATISTIC_RING_SIZE = 30 * 24 - 1; // Ring will contain hour data for latest 30.
                                                                // -1 means that the current hour is supposed
                                                                // to be taken from current statistic buffer
    private final IMap<UUID, StatisticRing> statisticRingMap;

    public SensorMetricsService(HazelcastInstance hazelcastInstance) {
        this.statisticRingMap = hazelcastInstance.getMap(CachingConfig.STATISTIC_RINGS_MAP);
    }

    @Override
    public void apply(StatisticSnapshot message) {
        StatisticRing statRing = statisticRingMap.getOrDefault(message.getSensorUUID(), new StatisticRing(STATISTIC_RING_SIZE));
        statRing.put(message.getHourUTCId(), new StatisticRing.StatisticValue(message.getMaxLevel(), message.getAvgLevel()));
        statisticRingMap.set(message.getSensorUUID(), statRing);
    }
}
