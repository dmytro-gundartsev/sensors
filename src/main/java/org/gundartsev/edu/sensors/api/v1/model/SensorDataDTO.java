package org.gundartsev.edu.sensors.api.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SensorDataDTO {
    @JsonProperty("co2")
    private int level;
    private OffsetDateTime time;
}
