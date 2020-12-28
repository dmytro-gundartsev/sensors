package org.gundartsev.edu.sensors.common.mq.registrars;

/**
 * Interface for the services - registrars, which only job is to stage the message into queue for async processing
 * by message fetchers @{@link org.gundartsev.edu.sensors.common.mq.fetchers.IMessageFetcher}
 *
 * @param <T>
 */
public interface IQueueItemRegistrar<T> {
    /**
     * Puts message into the queue
     *
     * @param data POJO to be put into queue as a message
     */
    void register(T data);
}
