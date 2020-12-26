package org.gundartsev.edu.sensors.status.stmachine;

import org.gundartsev.edu.sensors.domain.StatusData;
import org.gundartsev.edu.sensors.domain.alert.AlertEventEnum;

import java.time.OffsetDateTime;

public interface ISensorStatusStateMachine {
    AlertEventEnum accept(int co2Level, OffsetDateTime time);
    StatusData status();
}
