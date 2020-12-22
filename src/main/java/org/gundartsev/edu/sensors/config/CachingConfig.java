package org.gundartsev.edu.sensors.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CachingConfig {
    public static String SENSOR_MAP_VALUE = "sensorStatus";
    @Bean
    public Config config (){
        return augmentConfig(Config.load());
    }
    public Config augmentConfig(Config config) {
        MapConfig mapConfig = new MapConfig(SENSOR_MAP_VALUE)
                //    mapConfig.mapStoreConfig.isEnabled = true
                .setReadBackupData(false)
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setAsyncBackupCount(1)
                .setBackupCount(0);
        //mapConfig.mapStoreConfig.implementation = context.getBean("dataStoreMapStore")
        config.addMapConfig(mapConfig);
        return config;
    }
}
