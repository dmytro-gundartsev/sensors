package org.gundartsev.edu.sensors.common.listeners;

import com.hazelcast.core.HazelcastInstanceNotActiveException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
public class QueueItemFetcherFactory {
    private ThreadPoolTaskExecutor poolExecutor;
    private byte bossGroupThreadCount;

    public QueueItemFetcherFactory(ThreadPoolTaskExecutor poolExecutor, @Value("${bossGroup.threads.cnt:4}") Byte bossGroupThreadCount) {
        this.poolExecutor = poolExecutor;
        this.bossGroupThreadCount = bossGroupThreadCount;
    }

    public <T> IQueueItemFetcher createFetcher(BlockingQueue<T> queue, Consumer<T> itemConsumer) {
        return new IQueueItemFetcher() {
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
