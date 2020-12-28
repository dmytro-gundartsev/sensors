package org.gundartsev.edu.sensors.common.mq.fetchers;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Factory for creating message fetchers for Hazelcast queues
 */
@Service
public class HzMessageFetcherFactory implements IMessageFetcherFactory {
    private final ThreadPoolTaskExecutor poolExecutor; //shared thread pool executor to process retireved item frm queue
    private final byte bossGroupThreadCount; // configured number of boss group threads
    private final HazelcastInstance hkInstance;

    public HzMessageFetcherFactory(@Qualifier("fetcherSharedPool") ThreadPoolTaskExecutor poolExecutor, @Value("${bossGroup.threads.cnt:4}") Byte bossGroupThreadCount, HazelcastInstance hkInstance) {
        this.poolExecutor = poolExecutor;
        this.bossGroupThreadCount = bossGroupThreadCount;
        this.hkInstance = hkInstance;
    }

    public <T> IMessageFetcher<T> createFetcher(String queueName, Consumer<T> itemConsumer) {
        IQueue<T> queue = hkInstance.getQueue(queueName);
        return new IMessageFetcher<>() {
            private final ExecutorService bossThreadPool = Executors.newFixedThreadPool(bossGroupThreadCount);
            private boolean running = false;

            @Override
            public void start() {
                running = true;
                // start threads from boss group to listen the queue
                for (int i = 0; i < bossGroupThreadCount; i++) {
                    bossThreadPool.execute(() -> {
                        try {
                            while (running) {
                                T data = queue.take();
                                // once the item taken the boss thread will pass its execution to shared pool executor
                                // and will return to listening the queue
                                poolExecutor.execute(() -> itemConsumer.accept(data));
                            }
                        } catch (InterruptedException | HazelcastInstanceNotActiveException e) {
                            running = false; // more thorough problem analysis might be needed with more graceful
                            // shutdown of boss messages
                        }
                    });
                }
            }

            @Override
            public void stop() {
                this.running = false;
                bossThreadPool.shutdown();
            }
        };
    }
}
