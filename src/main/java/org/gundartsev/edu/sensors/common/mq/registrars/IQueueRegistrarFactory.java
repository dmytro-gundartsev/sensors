package org.gundartsev.edu.sensors.common.mq.registrars;

/**
 * Interface for creation of @{@link IQueueItemRegistrar}. Factory here is then for prbable easier replacement
 * of HzC queues for something else
 */
public interface IQueueRegistrarFactory {
    /**
     * Produce registrar for the named queue
     *
     * @param queueName Named queue
     * @param <T>       Type of the element in the queue
     * @return IQueueItemRegistrar implementation publishing messages into queue
     */
    <T> IQueueItemRegistrar<T> forQueue(String queueName);
}
