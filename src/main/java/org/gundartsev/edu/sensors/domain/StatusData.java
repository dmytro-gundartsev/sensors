package org.gundartsev.edu.sensors.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Entity represent status data needed for deciding a correct status transition. i.e. Momentum of {@link org.gundartsev.edu.sensors.status.stmachine.ISensorStatusStateMachine}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusData implements Serializable {
    StatusEnum status = StatusEnum.OK;      // current status
    OffsetDateTime latestMeasurementTime;   // latest measurement registered (for Info purpose in TZ of arrived data)
    int latestUTCMinuteId = 0;              // latest measurement registered in UTC TZ minute id (from Epoch)
    byte statusCounter = 0;                 // counter used in the transition logic of the state machine into/from ALERT status
}
