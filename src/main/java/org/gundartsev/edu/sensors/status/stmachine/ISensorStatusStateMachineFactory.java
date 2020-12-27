package org.gundartsev.edu.sensors.status.stmachine;

import org.gundartsev.edu.sensors.domain.StatusData;

public interface ISensorStatusStateMachineFactory {
    ISensorStatusStateMachine forStatus(StatusData data);
}
