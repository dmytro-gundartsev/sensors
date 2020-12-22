package org.gundartsev.edu.sensors.measurements.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.gundartsev.edu.sensors.config.CachingConfig;
import org.gundartsev.edu.sensors.domain.SensorData;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CachedMeasurementRegistrar implements ISensorMeasurementsRegistrar {
    private HazelcastInstance hazelcastInstance;

    public CachedMeasurementRegistrar(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void registerMeasurement(UUID uuid, SensorData data) {
        IMap map = hazelcastInstance.getMap(CachingConfig.SENSOR_MAP_VALUE);
        map.set(uuid, data);
    }
}
