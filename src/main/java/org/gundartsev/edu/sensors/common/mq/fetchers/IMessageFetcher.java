package org.gundartsev.edu.sensors.common.mq.fetchers;

/**
 * Fetcher for enqueued data
 * @param <T> Type of the element of the queue.
 */
public interface IMessageFetcher<T> {
    /**
     * Start listening of the queue for new messages
     */
    void start();
    /**
     * Stop listening of the queue for new messages
     */
    void stop();
}
