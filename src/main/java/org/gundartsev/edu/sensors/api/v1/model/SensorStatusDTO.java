package org.gundartsev.edu.sensors.api.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gundartsev.edu.sensors.domain.StatusEnum;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensorStatusDTO {
    @JsonProperty("status")
    private StatusEnum status;
}
