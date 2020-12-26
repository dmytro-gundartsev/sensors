package org.gundartsev.edu.sensors.common.registrars;

import org.gundartsev.edu.sensors.domain.MeasurementData;

import java.util.UUID;

public interface IQueueItemRegistrar<T> {
    void register(T data);
}
