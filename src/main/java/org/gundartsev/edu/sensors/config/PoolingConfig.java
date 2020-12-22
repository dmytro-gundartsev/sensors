package org.gundartsev.edu.sensors.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class PoolingConfig {
        @Bean
        ThreadPoolTaskExecutor executor(){
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(500);
            executor.setMaxPoolSize(1000);
            executor.setQueueCapacity(100);
            executor.setThreadNamePrefix("data-requests-slow-");
            executor.initialize();
            return executor;
        }
}
