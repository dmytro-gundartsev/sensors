package org.gundartsev.edu.sensors.api.v1.converter;

import org.gundartsev.edu.sensors.api.v1.model.SensorDataDTO;
import org.gundartsev.edu.sensors.domain.MeasurementData;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Convertor from dto {@link SensorDataDTO} of measurement into domain model {@link MeasurementData}
 */
@Component
public class SensorMeasurementV1Converter implements Converter<SensorDataDTO, MeasurementData> {
    @Override
    public MeasurementData convert(SensorDataDTO source) {
        return MeasurementData.builder().level(source.getLevel()).time(source.getTime()).build();
    }
}
