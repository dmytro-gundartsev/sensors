package org.gundartsev.edu.sensors.status.stmachine;

import org.gundartsev.edu.sensors.domain.StatusData;
import org.gundartsev.edu.sensors.domain.alert.AlertEventEnum;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;

/**
 * State machine for sensor status. The purpose: having current status and arriving data to decide if the transition
 * into new status needed, do that transition and update status + its service info
 */
public interface ISensorStatusStateMachine {
    /**
     * Accept measure decide on new status. If ALERT state lifecycle involved - produce the event
     * @param co2Level Measured level of CO2
     * @param dateTime Date-time for which the measure is sent by the sensor
     * @return AlertEventEnum in case of ALERT lifecyle involved: into or from ALERT status transition
     * or remaining in ALERT state. Null if no ALERT state lifecycle detected
     * @throws IllegalArgumentException in case of "late arrival". Unlikely situation of sensor trying to send
     * measurement for older timestamps.
     */
    @Nullable
    AlertEventEnum evaluate(int co2Level, OffsetDateTime dateTime) throws IllegalArgumentException;

    /**
     * Get updated status after new measurement based calculation
     * @return Updated status of the sensor after status reevaluation.
     */
    StatusData status();
}
