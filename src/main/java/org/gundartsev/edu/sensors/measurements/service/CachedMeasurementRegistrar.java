package org.gundartsev.edu.sensors.measurements.service;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import org.gundartsev.edu.sensors.config.CachingConfig;
import org.gundartsev.edu.sensors.domain.MeasurementData;
import org.gundartsev.edu.sensors.common.listeners.QueueItemFetcher;
import org.gundartsev.edu.sensors.common.listeners.QueueItemFetcherFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CachedMeasurementRegistrar implements ISensorMeasurementsRegistrar {
    private QueueItemFetcher measurementItemsFetcher;
    IQueue<MeasurementData> measurementQueue;

    public CachedMeasurementRegistrar(HazelcastInstance hazelcastInstance, QueueItemFetcherFactory fetcherFactory) {
        measurementQueue = hazelcastInstance.getQueue(CachingConfig.INCOMING_DATA_QUEUE);
    }

    @Override
    public void registerMeasurement(UUID uuid, MeasurementData data) {
        measurementQueue.add(data);
    }

}