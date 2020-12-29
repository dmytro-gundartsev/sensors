package org.gundartsev.edu.sensors.domain.metrics;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Pre-Aggregated statistic entity for a specified periodId (Id of hour or any timeperiod) which
 * is used for transfering as the message for queue and an element stored in metric buffer {@link MetricsBuffer}
 */

@Builder
@Getter
public class StatisticSnapshot implements Serializable {
    UUID sensorUUID; //UUID of the sensor
    int periodId; // id of the time period for which aggregated. Ex.: hourUTCId - number of Hour in UTC TZ from Epoch
    float avgLevel; // average C02 level within time period
    int maxLevel; // max level of CO2 within time period

    public void registerSensorUUID(UUID sensorUUID) {
        this.sensorUUID = sensorUUID;
    }

}
