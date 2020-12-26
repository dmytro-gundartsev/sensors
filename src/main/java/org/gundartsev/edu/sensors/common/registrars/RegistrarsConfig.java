package org.gundartsev.edu.sensors.common.registrars;

import org.gundartsev.edu.sensors.config.CachingConfig;
import org.gundartsev.edu.sensors.domain.MeasurementData;
import org.gundartsev.edu.sensors.domain.alert.AlertEvent;
import org.gundartsev.edu.sensors.domain.metrics.StatisticSnapshot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistrarsConfig {
    private final QueueRegistrarFactory factory;

    public RegistrarsConfig(QueueRegistrarFactory factory) {
        this.factory = factory;
    }

    @Bean
    IQueueItemRegistrar<MeasurementData> getMeasurementDataRegistrar(){
        return factory.forQueue(CachingConfig.INCOMING_DATA_QUEUE);
    }

    @Bean
    IQueueItemRegistrar<AlertEvent> getAlertEventRegistrar(){
        return factory.forQueue(CachingConfig.ALERT_DATA_QUEUE);
    }

    @Bean
    IQueueItemRegistrar<StatisticSnapshot> getStatisticSnapshotRegistrar(){
        return factory.forQueue(CachingConfig.STATISTIC_DATA_QUEUE);
    }

}
