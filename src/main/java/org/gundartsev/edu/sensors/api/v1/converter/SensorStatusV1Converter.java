package org.gundartsev.edu.sensors.api.v1.converter;

import org.gundartsev.edu.sensors.api.v1.model.SensorStatusDTO;
import org.gundartsev.edu.sensors.domain.MeasurementData;
import org.gundartsev.edu.sensors.domain.StatusData;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SensorStatusV1Converter implements Converter<StatusData, SensorStatusDTO> {
    @Override
    public SensorStatusDTO convert(StatusData source) {
        return new SensorStatusDTO(source.getStatus());
    }
}
