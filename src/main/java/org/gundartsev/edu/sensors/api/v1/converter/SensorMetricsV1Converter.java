package org.gundartsev.edu.sensors.api.v1.converter;

import org.gundartsev.edu.sensors.api.v1.model.SensorMetricsDTO;
import org.gundartsev.edu.sensors.api.v1.model.SensorStatusDTO;
import org.gundartsev.edu.sensors.domain.StatusData;
import org.gundartsev.edu.sensors.domain.metrics.StatisticValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Convertor from domain model of metrics data {@link StatisticValue} into dto {@link SensorMetricsDTO}
 */
@Component
public class SensorMetricsV1Converter implements Converter<StatisticValue, SensorMetricsDTO> {
    @Override
    public SensorMetricsDTO convert(StatisticValue source) {
        return new SensorMetricsDTO(source.getMaxValue(), Math.round(source.getAvgValue()));
    }
}
