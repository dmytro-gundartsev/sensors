package org.gundartsev.edu.sensors.api.v1.controller;

import org.gundartsev.edu.sensors.alert.ISensorAlertService;
import org.gundartsev.edu.sensors.api.v1.model.AlertDTO;
import org.gundartsev.edu.sensors.api.v1.model.SensorDataDTO;
import org.gundartsev.edu.sensors.api.v1.model.SensorMetricsDTO;
import org.gundartsev.edu.sensors.api.v1.model.SensorStatusDTO;
import org.gundartsev.edu.sensors.common.mq.registrars.IQueueItemRegistrar;
import org.gundartsev.edu.sensors.domain.MeasurementData;
import org.gundartsev.edu.sensors.domain.StatusData;
import org.gundartsev.edu.sensors.domain.metrics.MetricsValue;
import org.gundartsev.edu.sensors.metrics.ISensorMetricsService;
import org.gundartsev.edu.sensors.status.ISensorStatusService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.validation.Valid;
import java.util.Objects;
import java.util.UUID;

/**
 * Sensor API V1 controller.
 * Receives both Fast-Line measurements POST and Slow-Wit metrics/alerts retrieval
 */
@RestController
@RequestMapping("/api/v1")
public class SensorsControllerV1 {
    private final ConversionService conversionService;
    private final IQueueItemRegistrar<MeasurementData> measurementsRegistrar;
    private final ISensorStatusService statusService;
    private final Scheduler slowRunningTaskScheduler;
    private final ISensorMetricsService metricsService;
    private final ISensorAlertService alertService;

    public SensorsControllerV1(ConversionService conversionService, IQueueItemRegistrar<MeasurementData> measurementsRegistrar, ISensorStatusService statusService, @Qualifier("slowRequestSharedPool") AsyncListenableTaskExecutor threadPoolExecutor, ISensorMetricsService metricsService, ISensorAlertService alertService) {
        this.conversionService = conversionService;
        this.measurementsRegistrar = measurementsRegistrar;
        this.statusService = statusService;
        this.slowRunningTaskScheduler = Schedulers.fromExecutor(threadPoolExecutor);
        this.metricsService = metricsService;
        this.alertService = alertService;
    }

    /**
     * Serve-fast endpoint to facilitate lightning fast acceptance of POST measurements of sensors.
     * <br/>Type: Fast-line endpoint no pooling of the queries, assumes lighting-fast insertion into the HzC Queue
     *
     * @param uuid       UUID of the sensor
     * @param sensorData Sensor measurement data
     */
    @PostMapping(
            value = "sensors/{uuid}/measurements",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public Mono<Void> acceptMeasures(@PathVariable("uuid") UUID uuid, @RequestBody @Valid Mono<SensorDataDTO> sensorData) {
        return sensorData.doOnSuccess(data -> {
                    MeasurementData sData = conversionService.convert(data, MeasurementData.class);
                    Objects.requireNonNull(sData, "Improbable data conversion problem to [MeasurementData]");
                    Objects.requireNonNull(sData, "Improbable data conversion problem to [MeasurementData]");
                    sData.setUuid(uuid);
                    measurementsRegistrar.register(sData);
                }
        ).then();
    }

    /**
     * Retrieve status of the sensor, based on the last measurements send and state machine logic.
     * <br/>Type: Fast-line endpoint, no pooling of the queries, assumes lighting-fast HzC map data retrieval
     *
     * @param uuid Sensor's UUID
     * @return Status of the sensor {@link SensorStatusDTO}. Possible values: OK, WARN, ALERT
     */
    @GetMapping(
            value = "sensors/{uuid}/status",
            produces = {"application/json"}
    )
    public Mono<SensorStatusDTO> getSensorStatus(@PathVariable("uuid") UUID uuid) {
        return Mono.fromCallable(() -> {
            StatusData statusData = statusService.getStatus(uuid);
            SensorStatusDTO response = conversionService.convert(statusData, SensorStatusDTO.class);
            Objects.requireNonNull(response, "Improbable data conversion problem to [SensorStatusDTO]");
            return response;
        });
    }

    /**
     * Get last 30 days metrics of the sensor
     * <br/>Type: Slow-wit endpoint. Executed on separate ThreadPool, so no bottlenecking on few netty nio threads
     *
     * @param uuid Sensor's UUID
     * @return Sensor's metrics values {@link SensorMetricsDTO} containing average and max values.
     */
    @GetMapping(
            value = "sensors/{uuid}/metrics",
            produces = {"application/json"}
    )
    public Mono<SensorMetricsDTO> getSensorMetricsFor30Days(@PathVariable("uuid") UUID uuid) {
        return Mono.fromCallable(() -> {
            MetricsValue value = metricsService.getMetrics(uuid);
            SensorMetricsDTO dto = conversionService.convert(value, SensorMetricsDTO.class);
            Objects.requireNonNull(dto, "Improbable data conversion problem to [SensorMetricsDTO]");
            return dto;
        }).subscribeOn(slowRunningTaskScheduler);
    }

    /**
     * Get all alerts for the sensor
     * <br/>Type: Slow-wit endpoint. Executed on separate ThreadPool, so no bottlenecking on few netty nio threads
     *
     * @param uuid Sensor's UUID
     * @return All alerts of the sensor {@link AlertDTO}.
     */
    @GetMapping(
            value = "sensors/{uuid}/alerts",
            produces = {"application/json"}
    )
    public Flux<AlertDTO> getAllAlerts(@PathVariable("uuid") UUID uuid) {
        return alertService.getAlerts(uuid)
                .subscribeOn(slowRunningTaskScheduler)
                .map(val -> {
                    AlertDTO dto = conversionService.convert(val, AlertDTO.class);
                    Objects.requireNonNull(dto, "Improbable data conversion problem to [AlertDTO]");
                    return dto;
                });
    }
}
