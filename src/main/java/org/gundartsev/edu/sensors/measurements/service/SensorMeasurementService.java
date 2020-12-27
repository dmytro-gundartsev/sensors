package org.gundartsev.edu.sensors.measurements.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.gundartsev.edu.sensors.common.mq.fetchers.IMessageConsumingService;
import org.gundartsev.edu.sensors.common.mq.registrars.IQueueItemRegistrar;
import org.gundartsev.edu.sensors.config.CachingConfig;
import org.gundartsev.edu.sensors.domain.HourStatisticData;
import org.gundartsev.edu.sensors.domain.MeasurementData;
import org.gundartsev.edu.sensors.domain.SensorData;
import org.gundartsev.edu.sensors.domain.StatusData;
import org.gundartsev.edu.sensors.domain.alert.AlertEvent;
import org.gundartsev.edu.sensors.domain.alert.AlertEventEnum;
import org.gundartsev.edu.sensors.domain.metrics.StatisticSnapshot;
import org.gundartsev.edu.sensors.metrics.buffer.IRollingStatisticBufferEngine;
import org.gundartsev.edu.sensors.metrics.buffer.IRollingStatisticBufferEngineFactory;
import org.gundartsev.edu.sensors.status.stmachine.ISensorStatusStateMachine;
import org.gundartsev.edu.sensors.status.stmachine.ISensorStatusStateMachineFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.gundartsev.edu.sensors.metrics.buffer.IRollingStatisticBufferEngineFactory.BufferedTimeHorizon.HOUR;

@Service
public class SensorMeasurementService implements IMessageConsumingService<MeasurementData> {

    private final IMap<UUID, SensorData> sensorDataMap;
    private final IQueueItemRegistrar<StatisticSnapshot> statisticSnapshotRegistrar;
    private final IQueueItemRegistrar<AlertEvent> alertEventRegistrar;
    private final ISensorStatusStateMachineFactory stateMachineFactory;
    private final IRollingStatisticBufferEngineFactory statBufferEngineFactory;

    public SensorMeasurementService(HazelcastInstance hzcInstance, IQueueItemRegistrar<StatisticSnapshot> statisticSnapshotRegistrar, IQueueItemRegistrar<AlertEvent> alertEventRegistrar, ISensorStatusStateMachineFactory stateMachineFactory, IRollingStatisticBufferEngineFactory statBufferEngineFactory) {
        this.statisticSnapshotRegistrar = statisticSnapshotRegistrar;
        this.alertEventRegistrar = alertEventRegistrar;
        this.sensorDataMap = hzcInstance.getMap(CachingConfig.SENSOR_DATA_MAP_VALUE);
        this.stateMachineFactory = stateMachineFactory;
        this.statBufferEngineFactory = statBufferEngineFactory;
    }

    @Override
    public void apply(MeasurementData measurement) {
        SensorData sensorData = sensorDataMap.getOrDefault(measurement.getUuid(), new SensorData());
        sensorData.setStatusData(
                updateStatus(sensorData.getStatusData(), measurement.getLevel(), measurement.getTime()));
        sensorData.setRollingHourStatistic(
                rollingStatistic(measurement.getUuid(),
                        sensorData.getRollingHourStatistic(),
                        sensorData.getStatusData().getLatestUTCMinuteId(),
                        measurement.getLevel()));
        sensorDataMap.set(measurement.getUuid(), sensorData);
    }

    private StatusData updateStatus(StatusData data, int levelCo2, OffsetDateTime time) {
        ISensorStatusStateMachine stateMachine = stateMachineFactory.forStatus(data);
        AlertEventEnum alertEvent = stateMachine.accept(levelCo2, time);
        if (alertEvent != null) {
            alertEventRegistrar.register(new AlertEvent(time, alertEvent, levelCo2));
        }
        return stateMachine.status();
    }


    private HourStatisticData rollingStatistic(UUID sensorUUID, HourStatisticData hourStatistic, int currentUTCMinuteId, int levelCO2) {
        IRollingStatisticBufferEngine<HourStatisticData> engine = statBufferEngineFactory.getEngine(HOUR, hourStatistic);
        StatisticSnapshot snapshot = engine.accept(currentUTCMinuteId, levelCO2);
        if (snapshot != null) {
            snapshot.registerSensorUUID(sensorUUID);
            statisticSnapshotRegistrar.register(snapshot);
        }
        return engine.getBuffer();
    }
}
