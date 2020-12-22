package org.gundartsev.edu.sensors.api.v1.controller;

import org.gundartsev.edu.sensors.api.v1.model.SensorDataDTO;
import org.gundartsev.edu.sensors.domain.SensorData;
import org.gundartsev.edu.sensors.measurements.service.ISensorMeasurementsRegistrar;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SensorsControllerV1 {
    private final ConversionService conversionService;
    private final ISensorMeasurementsRegistrar measurementsRegistrar;

    public SensorsControllerV1(ConversionService conversionService, ISensorMeasurementsRegistrar measurementsRegistrar) {
        this.conversionService = conversionService;
        this.measurementsRegistrar = measurementsRegistrar;
    }

    @PostMapping(
            value = "sensors/{uuid}/measurements",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public Mono<Void> acceptMeasures(@PathVariable("uuid") UUID uuid, @RequestBody Mono<SensorDataDTO> sensorData) {
        return sensorData.doOnSuccess(data -> {
                    SensorData sData = conversionService.convert(data, SensorData.class);
                    measurementsRegistrar.registerMeasurement(uuid, sData);
                }
        ).then();
    }
}
