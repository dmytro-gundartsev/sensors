package org.gundartsev.edu.sensors.common.mq.fetchers;

import java.util.function.Consumer;

/**
 * Produce queue fetchers for named queues
 */
public interface IMessageFetcherFactory {
    /**
     * Create a fetcher for a named queue with given consumer of the item
     *
     * @param queueName    Named queue in HzC.
     * @param itemConsumer The logic to be called once the item arrives in the queue
     * @param <T>          Type of the item kept in queue
     * @return Constructed instance of the message fetcher
     */
    <T> IMessageFetcher<T> createFetcher(String queueName, Consumer<T> itemConsumer);
}