package org.gundartsev.edu.sensors.config;

import lombok.extern.slf4j.Slf4j;
import org.gundartsev.edu.sensors.common.mq.fetchers.IMessageConsumingService;
import org.gundartsev.edu.sensors.common.mq.fetchers.IMessageFetcher;
import org.gundartsev.edu.sensors.common.mq.fetchers.IMessageFetcherFactory;
import org.gundartsev.edu.sensors.domain.MeasurementData;
import org.gundartsev.edu.sensors.domain.alert.AlertEvent;
import org.gundartsev.edu.sensors.domain.metrics.StatisticSnapshot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class MessageFetchersConfig {
    private final IMessageFetcherFactory factory;
    private final List<IMessageFetcher> messageFetchers = new ArrayList<>(3);
    private final IMessageConsumingService<MeasurementData> measurementService;
    private final IMessageConsumingService<StatisticSnapshot> statisticService;
    private final IMessageConsumingService<AlertEvent> alertService;

    public MessageFetchersConfig(IMessageFetcherFactory factory,
                                 IMessageConsumingService<MeasurementData> measurementService,
                                 IMessageConsumingService<StatisticSnapshot> statisticService,
                                 IMessageConsumingService<AlertEvent> alertService) {
        this.factory = factory;
        this.measurementService = measurementService;
        this.statisticService = statisticService;
        this.alertService = alertService;
    }

    @Bean
    IMessageFetcher<MeasurementData> getMeasurementFetcher() {
        return createForQueue(CachingConfig.INCOMING_DATA_QUEUE, measurementService);
    }

    @Bean
    IMessageFetcher<AlertEvent> getAlertEventFetcher() {
        return createForQueue(CachingConfig.ALERT_DATA_QUEUE, alertService);
    }

    @Bean
    IMessageFetcher<StatisticSnapshot> getStatisticSnapshotIMessageFetcherFetcher() {
        return createForQueue(CachingConfig.STATISTIC_DATA_QUEUE, statisticService);
    }

    private <T> IMessageFetcher<T> createForQueue(String queueName, IMessageConsumingService<T> service) {
        IMessageFetcher<T> messageFetcher = factory.createFetcher(queueName, service::apply);
        messageFetcher.start();
        this.messageFetchers.add(messageFetcher);
        log.info("Message fetcher started for queue [{}]", queueName);
        return messageFetcher;
    }

    @PreDestroy
    void preDestroy() {
        this.messageFetchers.forEach(IMessageFetcher::stop);
    }

}
