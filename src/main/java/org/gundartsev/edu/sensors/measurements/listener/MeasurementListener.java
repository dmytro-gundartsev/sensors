package org.gundartsev.edu.sensors.measurements.listener;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.gundartsev.edu.sensors.config.CachingConfig;
import org.gundartsev.edu.sensors.domain.MeasurementData;
import org.gundartsev.edu.sensors.domain.SensorData;
import org.gundartsev.edu.sensors.common.listeners.QueueItemFetcher;
import org.gundartsev.edu.sensors.common.listeners.QueueItemFetcherFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.UUID;

@Component
public class MeasurementListener {
    private QueueItemFetcher measurementItemsFetcher;
    private IMap<UUID, Object> sensorsMap;

    public MeasurementListener(QueueItemFetcherFactory measurementItemsFetcherFactory, HazelcastInstance hazelcastInstance) {
        IQueue<MeasurementData> measurementQueue = hazelcastInstance.getQueue(CachingConfig.INCOMING_DATA_QUEUE);
        sensorsMap = hazelcastInstance.getMap(CachingConfig.SENSOR_MAP_VALUE);
        this.measurementItemsFetcher = measurementItemsFetcherFactory.createFetcher(measurementQueue, this::process);
    }

    @PostConstruct
    void onCreate(){
        measurementItemsFetcher.start();
    }

    @PreDestroy
    void shutdown(){
        measurementItemsFetcher.stop();
    }
    public void process(MeasurementData data){
        sensorsMap.put(data.getUuid(), new SensorData());
    }
}
