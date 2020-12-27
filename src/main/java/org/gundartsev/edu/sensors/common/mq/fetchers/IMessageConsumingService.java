package org.gundartsev.edu.sensors.common.mq.fetchers;

public interface IMessageConsumingService<T> {
    void apply(T message);
}
