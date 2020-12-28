package org.gundartsev.edu.sensors.domain.alert;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Alert event produced by the {@link org.gundartsev.edu.sensors.status.stmachine.ISensorStatusStateMachine}
 * and to be buffered and later to form Alert entity
 */
@AllArgsConstructor
@Getter
public class AlertEvent implements Serializable {
    OffsetDateTime dateTime; // when arrived
    AlertEventEnum eventType; // time of alert event: START of alerting, ALERTing and STOP alerting.
    int value; // level of CO2
}
