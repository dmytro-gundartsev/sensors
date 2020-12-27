package org.gundartsev.edu.sensors.alert;

import org.gundartsev.edu.sensors.common.mq.fetchers.IMessageConsumingService;
import org.gundartsev.edu.sensors.domain.alert.AlertEvent;
import org.springframework.stereotype.Service;

@Service
public class SensorAlertService implements IMessageConsumingService<AlertEvent> {
    @Override
    public void apply(AlertEvent message) {

    }
}
