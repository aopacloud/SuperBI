package net.aopacloud.superbi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: rick
 * @date: 2024/1/18 17:26
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    private int CORE_POOL_SIZE = 10;
    private int MAX_POOL_SIZE = 100;
    private int QUEUE_CAPACITY = 500;

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public Executor getAsyncExecutor() {
        threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        threadPoolTaskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        threadPoolTaskExecutor.setQueueCapacity(QUEUE_CAPACITY);
        threadPoolTaskExecutor.setThreadNamePrefix("async-thread-pool-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    @PreDestroy
    public void destroy() {
        if (threadPoolTaskExecutor != null) {
            threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
            threadPoolTaskExecutor.setAwaitTerminationSeconds(30);
            threadPoolTaskExecutor.shutdown();
            try {
                if (!threadPoolTaskExecutor.getThreadPoolExecutor().awaitTermination(30, TimeUnit.SECONDS)) {
                    log.warn("thread pool did not shutdown gracefully within 30 seconds.");
                    threadPoolTaskExecutor.getThreadPoolExecutor().shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("thread pool interrupted when shutting down.");
                Thread.currentThread().interrupt();
            }
        }
    }
}
