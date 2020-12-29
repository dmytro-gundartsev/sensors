package org.gundartsev.edu.sensors.api.v1.converter;

import org.gundartsev.edu.sensors.api.v1.model.SensorMetricsDTO;
import org.gundartsev.edu.sensors.domain.metrics.MetricsValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Convertor from domain model of metrics data {@link MetricsValue} into dto {@link SensorMetricsDTO}
 */
@Component
public class SensorMetricsV1Converter implements Converter<MetricsValue, SensorMetricsDTO> {
    @Override
    public SensorMetricsDTO convert(MetricsValue source) {
        return new SensorMetricsDTO(source.getMaxValue(), Math.round(source.getAvgValue()));
    }
}
