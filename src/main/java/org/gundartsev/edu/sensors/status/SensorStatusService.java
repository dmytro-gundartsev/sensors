package org.gundartsev.edu.sensors.status;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.gundartsev.edu.sensors.common.exception.SensorNotFoundException;
import org.gundartsev.edu.sensors.config.IMDGStorageConfig;
import org.gundartsev.edu.sensors.domain.SensorData;
import org.gundartsev.edu.sensors.domain.StatusData;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SensorStatusService implements ISensorStatusService {
    private IMap<UUID, SensorData> sensorDataMap;

    public SensorStatusService(HazelcastInstance hkInstance) {
        this.sensorDataMap = hkInstance.getMap(IMDGStorageConfig.SENSOR_DATA_MAP);
    }

    @Override
    public StatusData getStatus(UUID uuid) {
        SensorData data = sensorDataMap.get(uuid);
        if (data == null) {
            throw new SensorNotFoundException(uuid);
        } else {
            return data.getStatusData();
        }
    }
}
