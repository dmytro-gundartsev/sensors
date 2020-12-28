package org.gundartsev.edu.sensors.api.v1.model;

import lombok.Data;

import java.util.HashMap;

@Data
public class AlertDTO extends HashMap<String, Object> {
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
    public static final String MEASUREMENT_PREFIX = "measurement";
}
