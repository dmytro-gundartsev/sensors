package org.gundartsev.edu.sensors.domain.alert;

/**
 * Type of the alert event found by {@link org.gundartsev.edu.sensors.status.stmachine.ISensorStatusStateMachine}
 */
public enum AlertEventEnum {
    START_ALERT, // first measure > 2000 registered in a new ALERT state
    ALERT,  // ALERT state is raised, consequent measure > 2000 arrived
    STOP_ALERT // Detected event of clearing ALERT event from sensor
}
