package org.gundartsev.edu.sensors.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.QueueConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @Bean
    public Config config (){
        return augmentConfig(Config.load());
    }

    /**
     * Augment default config of Hazelcast
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
}
