package org.gundartsev.edu.sensors.measurements.listener;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import org.gundartsev.edu.sensors.config.CachingConfig;
import org.gundartsev.edu.sensors.domain.MeasurementData;
import org.gundartsev.edu.sensors.common.listeners.IQueueItemFetcher;
import org.gundartsev.edu.sensors.common.listeners.QueueItemFetcherFactory;
import org.gundartsev.edu.sensors.measurements.service.SensorMeasurementService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class MeasurementListener {
    private IQueueItemFetcher measurementItemsFetcher;
    private SensorMeasurementService service;

    public MeasurementListener(HazelcastInstance hazelcastInstance, QueueItemFetcherFactory measurementItemsFetcherFactory, SensorMeasurementService service, SensorMeasurementService service1) {
        this.service = service1;
        IQueue<MeasurementData> measurementQueue = hazelcastInstance.getQueue(CachingConfig.INCOMING_DATA_QUEUE);
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
        service.apply(data);
    }
}
