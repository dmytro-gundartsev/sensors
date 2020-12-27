package org.gundartsev.edu.sensors.common.mq.registrars;

public interface IQueueItemRegistrar<T> {
    void register(T data);
}
