package be.ap.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configures application-level thread pools for asynchronous task execution.
 *
 * <p>
 * Defines separate executors for lookup operations and sequential queue-based
 * tasks.
 * </p>
 */
@Configuration
public class AsyncConfig {

    /**
     * Thread pool for concurrent lookup operations.
     *
     * <p>
     * Uses a bounded queue with a CallerRunsPolicy to avoid task rejection under
     * load.
     * </p>
     *
     * @return executor used by @Async("lookupExecutor") methods
     */
    @Bean(name = "lookupExecutor")
    public Executor lookupExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("lookup-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Single-threaded executor for sequential background task processing.
     *
     * <p>
     * Ensures FIFO execution for queued operations with controlled throughput.
     * </p>
     *
     * @return executor used for serialized background tasks
     */
    @Bean(name = "queueTaskExecutor")
    public Executor queueTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(250);
        executor.setThreadNamePrefix("task-queue-");
        executor.initialize();
        return executor;
    }
}