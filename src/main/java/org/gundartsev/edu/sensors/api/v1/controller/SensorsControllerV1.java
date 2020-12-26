package org.gundartsev.edu.sensors.api.v1.controller;

import org.gundartsev.edu.sensors.api.v1.model.SensorDataDTO;
import org.gundartsev.edu.sensors.api.v1.model.SensorStatusDTO;
import org.gundartsev.edu.sensors.common.registrars.IQueueItemRegistrar;
import org.gundartsev.edu.sensors.domain.MeasurementData;
import org.gundartsev.edu.sensors.domain.StatusData;
import org.gundartsev.edu.sensors.status.ISensorStatusService;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SensorsControllerV1 {
    private final ConversionService conversionService;
    private final IQueueItemRegistrar<MeasurementData> measurementsRegistrar;
    private final ISensorStatusService statusService;

    public SensorsControllerV1(ConversionService conversionService, IQueueItemRegistrar<MeasurementData> measurementsRegistrar, ISensorStatusService statusService) {
        this.conversionService = conversionService;
        this.measurementsRegistrar = measurementsRegistrar;
        this.statusService = statusService;
    }

    @PostMapping(
            value = "sensors/{uuid}/measurements",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public Mono<Void> acceptMeasures(@PathVariable("uuid") UUID uuid, @RequestBody Mono<SensorDataDTO> sensorData) {
        return sensorData.doOnSuccess(data -> {
                    MeasurementData sData = conversionService.convert(data, MeasurementData.class);
                    sData.setUuid(uuid);
                    measurementsRegistrar.register(sData);
                }
        ).then();
    }

    @GetMapping(
            value = "sensors/{uuid}/status",
            produces = {"application/json"}
    )
    public Mono<SensorStatusDTO> getSensorStatus(@PathVariable("uuid") UUID uuid) {
        StatusData statusData = statusService.getStatus(uuid);
        SensorStatusDTO response = conversionService.convert(statusData, SensorStatusDTO.class);
        if (response == null) {
            throw new IllegalStateException("Conversion of status with UUID" + uuid + " failed");
        }
        return Mono.just(response); // fast API so no need in parallelism here
    }
}
