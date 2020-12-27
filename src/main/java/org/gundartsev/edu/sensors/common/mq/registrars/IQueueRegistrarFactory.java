package org.gundartsev.edu.sensors.common.mq.registrars;

public interface IQueueRegistrarFactory {
    <T> IQueueItemRegistrar<T> forQueue(String queueName);
}
