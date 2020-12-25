package org.gundartsev.edu.sensors.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class StatusData implements Serializable {
    StatusEnum status = StatusEnum.OK;
    byte statusCounter = 0;
}
