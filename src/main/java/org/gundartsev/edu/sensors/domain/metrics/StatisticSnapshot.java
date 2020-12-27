package org.gundartsev.edu.sensors.domain.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Builder
@Getter
public class StatisticSnapshot implements Serializable {
    UUID sensorUUID;
    int hourUTCId;
    float avgLevel;
    int maxLevel;
    public void registerSensorUUID(UUID sensorUUID){
        this.sensorUUID = sensorUUID;
    }
}
