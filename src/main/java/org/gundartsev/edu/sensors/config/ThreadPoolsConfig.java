package org.gundartsev.edu.sensors.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configure Thread pools executors for shared parallel processing
 */
@Configuration
public class ThreadPoolsConfig {
    /**
     * Create executor which serve slow-wit logic (metrics and alerts) to unblock netty-nio threads
     */
    @Bean("slowRequestSharedPool")
    ThreadPoolTaskExecutor executorForSlowAPIRequests() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(200);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("data-requests-slow-");
        executor.initialize();
        return executor;
    }

    /**
     * Create executor shared among fetchers for processing of messages coming from queues
     */
    @Bean("fetcherSharedPool")
    ThreadPoolTaskExecutor executorForFetchers() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(200);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("fetcher-");
        executor.initialize();
        return executor;
    }

}
