package org.gundartsev.edu.sensors.measurements.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.gundartsev.edu.sensors.common.registrars.IQueueItemRegistrar;
import org.gundartsev.edu.sensors.config.CachingConfig;
import org.gundartsev.edu.sensors.domain.*;
import org.gundartsev.edu.sensors.domain.alert.AlertEvent;
import org.gundartsev.edu.sensors.domain.alert.AlertEventEnum;
import org.gundartsev.edu.sensors.domain.metrics.StatisticSnapshot;
import org.gundartsev.edu.sensors.metrics.RollingHourStatisticBufferEngine;
import org.gundartsev.edu.sensors.status.stmachine.ISensorStatusStateMachine;
import org.gundartsev.edu.sensors.status.stmachine.SensorStatusStateMachineFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class SensorMeasurementService implements ISensorMeasurementService {

    private final IMap<UUID, SensorData> sensorDataMap;
    private final IQueueItemRegistrar<StatisticSnapshot> statisticSnapshotRegistrar;
    private final IQueueItemRegistrar<AlertEvent> alertEventRegistrar;

    public SensorMeasurementService(HazelcastInstance hzcInstance, IQueueItemRegistrar<StatisticSnapshot> statisticSnapshotRegistrar, IQueueItemRegistrar<AlertEvent> alertEventRegistrar) {
        this.statisticSnapshotRegistrar = statisticSnapshotRegistrar;
        this.alertEventRegistrar = alertEventRegistrar;
        this.sensorDataMap = hzcInstance.getMap(CachingConfig.SENSOR_DATA_MAP_VALUE);
    }

    @Override
    public void apply(MeasurementData measurement) {
        SensorData sensorData = sensorDataMap.getOrDefault(measurement.getUuid(), new SensorData());
        sensorData.setStatusData(
                updateStatus(sensorData.getStatusData(), measurement.getLevel(), measurement.getTime()));
        sensorData.setRollingHourStatistic(
                rollingStatistic(sensorData.getRollingHourStatistic(), sensorData.getStatusData().getLatestUTCMinuteId(), measurement.getLevel()));
        sensorDataMap.set(measurement.getUuid(), sensorData);
    }

    private StatusData updateStatus(StatusData data, int levelCo2, OffsetDateTime time){
        ISensorStatusStateMachine stateMachine = SensorStatusStateMachineFactory.forStatus(data);
        AlertEventEnum alertEvent = stateMachine.accept(levelCo2, time);
        if (alertEvent != null){
            alertEventRegistrar.register(new AlertEvent(time, alertEvent, levelCo2));
        }
        return stateMachine.status();
    }


    private HourStatistic rollingStatistic(HourStatistic hourStatistic, int currentUTCMinuteId, int levelCO2){
        RollingHourStatisticBufferEngine engine = RollingHourStatisticBufferEngine.engineForBuffer(hourStatistic);
        StatisticSnapshot snapshot = engine.accept(currentUTCMinuteId, levelCO2);
        if (snapshot != null){
            statisticSnapshotRegistrar.register(snapshot);
        }
        return engine.getBuffer();
    }
}
