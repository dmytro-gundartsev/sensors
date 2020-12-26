package org.gundartsev.edu.sensors.status.stmachine;

import org.gundartsev.edu.sensors.domain.StatusData;
import org.springframework.stereotype.Service;

@Service
public class SensorStatusStateMachineFactory {
    public static ISensorStatusStateMachine forStatus(StatusData data){
        return new SensorStatusStateMachine(data);
    }
}
