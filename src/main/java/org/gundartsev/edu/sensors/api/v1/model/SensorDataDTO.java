package org.gundartsev.edu.sensors.api.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.OffsetDateTime;

@Data
public class SensorDataDTO {
    @JsonProperty("co2")
    @Min(0)
    @Max(100_000) // I believe it's reasonable upper limit
    private int level;
    private OffsetDateTime time;
}
