package org.gundartsev.edu.sensors.status;

import com.hazelcast.map.IMap;
import org.gundartsev.edu.sensors.common.exception.SensorNotFoundException;
import org.gundartsev.edu.sensors.domain.SensorData;
import org.gundartsev.edu.sensors.domain.StatusData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SensorStatusService implements ISensorStatusService {
    private final IMap<UUID, SensorData> sensorDataMap;

    public SensorStatusService(IMap<UUID, SensorData> sensorDataMap) {
        this.sensorDataMap = sensorDataMap;
    }

    @NotNull
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
