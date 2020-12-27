package org.gundartsev.edu.sensors.common.mq.fetchers;

public interface IMessageFetcher<T> {
    void start();
    void stop();
}
