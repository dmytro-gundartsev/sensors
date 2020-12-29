package org.gundartsev.edu.sensors.api.v1.converter;

import org.gundartsev.edu.sensors.api.v1.model.AlertDTO;
import org.gundartsev.edu.sensors.domain.alert.Alert;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Convertor from domain model of metrics data {@link Alert} into dto {@link AlertDTO}
 */
@Component
public class AlertDataV1Converter implements Converter<Alert, AlertDTO> {
    @Override
    public AlertDTO convert(Alert source) {
        AlertDTO dto = new AlertDTO();
        dto.put(AlertDTO.START_TIME, source.getStartTime());
        dto.put(AlertDTO.END_TIME, source.getEndTime());
        for (int idx = 0; idx < source.getMeasurements().size(); idx++) {
            dto.put(AlertDTO.MEASUREMENT_PREFIX + (idx + 1), source.getMeasurements().get(idx));
        }
        return dto;
    }
}
