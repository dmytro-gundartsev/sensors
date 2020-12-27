package org.gundartsev.edu.sensors.common.mq.fetchers;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
public class MessageFetcherFactory implements IMessageFetcherFactory {
    private ThreadPoolTaskExecutor poolExecutor;
    private byte bossGroupThreadCount;
    private HazelcastInstance hkInstance;

    public MessageFetcherFactory(ThreadPoolTaskExecutor poolExecutor, @Value("${bossGroup.threads.cnt:4}") Byte bossGroupThreadCount, HazelcastInstance hkInstance) {
        this.poolExecutor = poolExecutor;
        this.bossGroupThreadCount = bossGroupThreadCount;
        this.hkInstance = hkInstance;
    }

    public <T> IMessageFetcher<T> createFetcher(String queueName, Consumer<T> itemConsumer) {
        IQueue<T> queue = hkInstance.getQueue(queueName);
        return new IMessageFetcher<T>() {
            private ExecutorService bossThreadPool = Executors.newFixedThreadPool(bossGroupThreadCount);
            private boolean running = false;

            @Override
            public void start() {
                running = true;
                for (int i = 0; i < bossGroupThreadCount; i++) {
                    bossThreadPool.execute(() -> {
                        try {
                            while (running) {
                                T data = queue.take();
                                poolExecutor.execute(() -> itemConsumer.accept(data));
                            }
                        } catch (InterruptedException| HazelcastInstanceNotActiveException e) {
                           running = false;
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
