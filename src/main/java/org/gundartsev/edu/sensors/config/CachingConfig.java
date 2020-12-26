package org.gundartsev.edu.sensors.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.QueueConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CachingConfig {
    public static String SENSOR_STATUS_MAP = "sensorStatus";
    public static String SENSOR_DATA_MAP_VALUE = "sensorStatus";
    public static String INCOMING_DATA_QUEUE = "measurementsQueue";
    public static String ALERT_DATA_QUEUE = "alertEventQueue";
    public static String STATISTIC_DATA_QUEUE = "statisticQueue";
    @Bean
    public Config config (){
        return augmentConfig(Config.load());
    }
    public Config augmentConfig(Config config) {
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
        MapConfig mapConfig = new MapConfig(SENSOR_STATUS_MAP)
                //    mapConfig.mapStoreConfig.isEnabled = true
                .setReadBackupData(false)
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setAsyncBackupCount(1)
                .setBackupCount(0);
        //mapConfig.mapStoreConfig.implementation = context.getBean("dataStoreMapStore")
        config.addMapConfig(mapConfig);
        config.addQueueConfig(measureQueueConf);
        config.addQueueConfig(alertQueueConf);
        config.addQueueConfig(statisticQueueConf);
        return config;
    }
}
