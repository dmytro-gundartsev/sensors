package org.gundartsev.edu.sensors.status.stmachine;

import org.gundartsev.edu.sensors.domain.StatusData;
import org.gundartsev.edu.sensors.domain.StatusEnum;
import org.gundartsev.edu.sensors.domain.alert.AlertEventEnum;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

class SensorStatusStateMachine implements ISensorStatusStateMachine{
    private static final int SECONDS_IN_MINUTE = 60;
    public static final int DANGEROUS_CO2_LEVEL_THRESHOLD = 2000;
    public static final byte DISTRESS_LEVEL_CHANGE_COUNTER = 3;

    private final StatusData statusData;

    public SensorStatusStateMachine(StatusData statusData) {
        this.statusData = statusData;
    }

    @Override
    public AlertEventEnum evaluate(int co2Level, OffsetDateTime dateTime) {
        if (statusData.getLatestMeasurementTime() != null && statusData.getLatestMeasurementTime().isAfter(dateTime)){
            throw new IllegalArgumentException("Sensor sent older measurement as for ["+dateTime+"] while having" +
                    "already registered data for ["+ statusData.getLatestMeasurementTime() +"]");
        }
        AlertEventEnum alertEvent = null;
        int statCounter = statusData.getStatusCounter();
        StatusEnum status = statusData.getStatus();
        if (co2Level > DANGEROUS_CO2_LEVEL_THRESHOLD){
            // OK -> WARN
            if (status == StatusEnum.OK){
                status = StatusEnum.WARN;
                statCounter = 1;
                // WARN --> WARN (inc counter) -(counter>=3)-> ALERT
            } else if (status == StatusEnum.WARN){
                if (++statCounter >= DISTRESS_LEVEL_CHANGE_COUNTER) {
                    status = StatusEnum.ALERT;
                    alertEvent = AlertEventEnum.START_ALERT;
                    statCounter = DISTRESS_LEVEL_CHANGE_COUNTER;
                }
                // remain on ALERT
            } else {
                alertEvent = AlertEventEnum.ALERT;
                statCounter = DISTRESS_LEVEL_CHANGE_COUNTER;
            }
        } else {
            // ALERT --> ALERT (dec counter) -(counter<=0)-> OK
            if (status == StatusEnum.ALERT){
                if (--statCounter <= 0){
                    status = StatusEnum.OK;
                    alertEvent = AlertEventEnum.STOP_ALERT;
                    statCounter = 0;
                }
                // [WARN|OK] --> OK
            } else {
                status = StatusEnum.OK;
                statCounter = 0;
            }
        }
        statusData.setStatus(status);
        statusData.setStatusCounter((byte)statCounter);
        statusData.setLatestUTCMinuteId(calculateUTCMinuteId(dateTime));
        statusData.setLatestMeasurementTime(dateTime);
        return alertEvent;
    }

    private int calculateUTCMinuteId (OffsetDateTime offsetTime){
        // getting current minute number from Epoch, int value is safe for the next centuries.
        return (int)(offsetTime.withOffsetSameInstant(ZoneOffset.UTC).toEpochSecond() / SECONDS_IN_MINUTE);
    }

    @Override
    public StatusData status() {
        return statusData;
    }
}
