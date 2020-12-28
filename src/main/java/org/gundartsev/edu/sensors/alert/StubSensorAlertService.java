package org.gundartsev.edu.sensors.alert;

import org.gundartsev.edu.sensors.common.mq.fetchers.IMessageConsumingService;
import org.gundartsev.edu.sensors.domain.alert.Alert;
import org.gundartsev.edu.sensors.domain.alert.AlertEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Stub implementation of the Alert service.
 * <br/>Main ideas for the real implementation:
 * <br/> - Collected alert messages to be buffered (as for Statistic collections) in IMap<UUID, AlertBuffer>
 *     (similar to statistic buffering) and once per hour (or when AlertEnum.STOP arrives) flushed => guaranteed at least 60 times less intense data flow,
 *     comparing to no-buffered implementation
 * <br/> - Retrieval of the alerts has to be done directly from NoSQl data storage(Spring Data Repository will help)
 *      small adjustments to be done with the data currently in the buffer (the same approach as for statistic).
 *      storage with no caching on the way
 */
@Service
public class StubSensorAlertService implements IMessageConsumingService<AlertEvent>, ISensorAlertService {
    @Override
    public void apply(AlertEvent message) {
        // do nothing;
    }

    @Override
    public Flux<Alert> getAlerts(UUID sensorUUID) {
        //
        return Flux.just(
                Alert.builder()
                        .endTime(OffsetDateTime.now())
                        .startTime(OffsetDateTime.now().minusDays(2))
                        .measurements(List.of(2100,2200,2300,2100))
                .build()
        );
    }
}
