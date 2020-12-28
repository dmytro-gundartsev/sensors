package org.gundartsev.edu.sensors.common.mq.registrars;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import org.gundartsev.edu.sensors.domain.MeasurementData;
import org.springframework.stereotype.Service;

/**
 * Produce registrars for HzC queues
 */
@Service
public class HzQueueRegistrarFactory implements IQueueRegistrarFactory {
    private final HazelcastInstance hkInstance;
    IQueue<MeasurementData> measurementQueue;

    public HzQueueRegistrarFactory(HazelcastInstance hazelcastInstance) {
        this.hkInstance = hazelcastInstance;
    }

    public <T> IQueueItemRegistrar<T> forQueue(String queueName) {
        IQueue<T> foundQueue = hkInstance.getQueue(queueName);
        return new IQueueItemRegistrar<T>() {
            private IQueue<T> queue = foundQueue;

            @Override
            public void register(T data) {
                queue.add(data);
            }
        };
    }
}