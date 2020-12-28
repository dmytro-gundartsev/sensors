package org.gundartsev.edu.sensors.domain.alert;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Represents Sensor Alert entity. This entity register moment of first and following CO2 levels > 2000 with times
 * of first and last registered measures.
 */
@Data
@Builder
public class Alert {
    OffsetDateTime startTime; // startTime is the time when first measurement > 2000 within the alert state arrived
    OffsetDateTime endTime; // endTime is the time when last measurement > 2000 within the alert state arrived
    List<Integer> measurements; // sequence of the registered >200 levels of CO2 within alert state
}
