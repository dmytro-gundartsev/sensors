package org.gundartsev.edu.sensors.domain.alert;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@Getter
public class AlertEvent implements Serializable {
    OffsetDateTime dateTime;
    AlertEventEnum eventType;
    int value;
}
