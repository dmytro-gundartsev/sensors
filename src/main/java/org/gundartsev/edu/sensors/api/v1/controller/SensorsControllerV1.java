package org.gundartsev.edu.sensors.api.v1.controller;

import org.gundartsev.edu.sensors.api.v1.model.SensorDataDTO;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SensorsControllerV1 {
    @PostMapping(
            value = "sensors/{uuid}/measurements",
            produces = { "application/json" },
            consumes = { "application/json" }
    )
    public Mono<Void> acceptMeasures(@PathVariable("uuid") UUID uuid, @RequestBody Mono<SensorDataDTO> sensorData){
        return Mono.empty();
    }
}
