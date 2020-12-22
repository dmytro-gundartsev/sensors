package org.gundartsev.edu.sensors.api.v1.converter;

import org.gundartsev.edu.sensors.api.v1.model.SensorDataDTO;
import org.gundartsev.edu.sensors.domain.SensorData;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SensorDataV1Converter implements Converter<SensorDataDTO, SensorData> {
    @Override
    public SensorData convert(SensorDataDTO source) {
        return SensorData.builder().level(source.getLevel()).time(source.getTime()).build();
    }
}
