package org.gundartsev.edu.sensors.status.stmachine;

import org.gundartsev.edu.sensors.domain.StatusData;
import org.springframework.stereotype.Service;

@Service
public class SensorStatusStateMachineFactory implements ISensorStatusStateMachineFactory {
    @Override
    public ISensorStatusStateMachine forStatus(StatusData data) {
        return new SensorStatusStateMachine(data);
    }
}
