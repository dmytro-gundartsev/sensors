package org.gundartsev.edu.sensors.common.mq.fetchers;

import java.util.function.Consumer;

public interface IMessageFetcherFactory {
    <T> IMessageFetcher<T> createFetcher(String queueName, Consumer<T> itemConsumer);
}