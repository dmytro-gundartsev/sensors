package org.gundartsev.edu.sensors.measurements.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.gundartsev.edu.sensors.config.CachingConfig;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class CachedMeasurementRegistrar implements ISensorMeasurementsRegistrar{
    private HazelcastInstance hazelcastInstance;

    public CachedMeasurementRegistrar(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void registerMeasurement(UUID uuid, OffsetDateTime time, int value) {
        IMap map = hazelcastInstance.getMap(CachingConfig.SENSOR_MAP_VALUE);
        map.set(uuid, );
    }
}
