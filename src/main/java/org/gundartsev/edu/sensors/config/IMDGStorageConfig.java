package org.gundartsev.edu.sensors.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.gundartsev.edu.sensors.domain.SensorData;
import org.gundartsev.edu.sensors.domain.alert.AlertsBuffer;
import org.gundartsev.edu.sensors.domain.metrics.MetricsBuffer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
/**
 * Configures of IMDG data storage, based on Hazelcast
 */
public class IMDGStorageConfig {
    public static final String SENSOR_DATA_MAP = "sensorData";
    public static final String METRIC_BUFFER_MAP = "metricBuffer";
    public static final String ALERT_BUFFER_MAP = "alertBuffer";
    public static final String INCOMING_DATA_QUEUE = "measurementsQueue";
    public static final String ALERT_DATA_QUEUE = "alertEventQueue";
    public static final String STATISTIC_DATA_QUEUE = "statisticQueue";

    public Config config() {
        return augmentConfig(Config.load());
    }

    /**
     * Augment default config of Hazelcast
     *
     * @param config default configuration (taken from hazelcast.xml)
     * @return Augmented configuration with configs of queues and maps
     */
    public Config augmentConfig(Config config) {
        // duplicities here are for possible separate fine configuration of the queues and maps
        QueueConfig measureQueueConf = new QueueConfig(INCOMING_DATA_QUEUE)
                .setBackupCount(1)
                .setEmptyQueueTtl(-1)
                .setAsyncBackupCount(0);
        QueueConfig statisticQueueConf = new QueueConfig(STATISTIC_DATA_QUEUE)
                .setBackupCount(1)
                .setEmptyQueueTtl(-1)
                .setAsyncBackupCount(0);
        QueueConfig alertQueueConf = new QueueConfig(ALERT_DATA_QUEUE)
                .setBackupCount(1)
                .setEmptyQueueTtl(-1)
                .setAsyncBackupCount(0);
        MapConfig metricBufferMapConfig = new MapConfig(METRIC_BUFFER_MAP)
                .setReadBackupData(false)
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setAsyncBackupCount(1)
                .setBackupCount(0);
        MapConfig sensorStatusMapConfig = new MapConfig(SENSOR_DATA_MAP)
                .setReadBackupData(false)
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setAsyncBackupCount(1)
                .setBackupCount(0);
        MapConfig alertBufferMapConfig = new MapConfig(ALERT_BUFFER_MAP)
                .setReadBackupData(false)
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setAsyncBackupCount(1)
                .setBackupCount(0);
        config.addMapConfig(metricBufferMapConfig);
        config.addMapConfig(sensorStatusMapConfig);
        config.addMapConfig(alertBufferMapConfig);
        config.addQueueConfig(measureQueueConf);
        config.addQueueConfig(alertQueueConf);
        config.addQueueConfig(statisticQueueConf);
        return config;
    }

    @Bean
    HazelcastInstance getInstance() {
        return Hazelcast.newHazelcastInstance(config());
    }

    @Bean
    IMap<UUID, SensorData> getSensorDataMap(HazelcastInstance instance) {
        return instance.getMap(SENSOR_DATA_MAP);
    }
    @Bean
    IMap<UUID, MetricsBuffer> getMetricsBufferMap(HazelcastInstance instance) {
        return instance.getMap(METRIC_BUFFER_MAP);
    }
    @Bean
    IMap<UUID, AlertsBuffer> getAlertsBufferMap(HazelcastInstance instance) {
        return instance.getMap(ALERT_BUFFER_MAP);
    }

}
