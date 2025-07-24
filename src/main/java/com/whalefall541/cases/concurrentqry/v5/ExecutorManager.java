package com.whalefall541.cases.concurrentqry.v5;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@AllArgsConstructor
public class ExecutorManager {
    // 只注入原生 ExecutorService 类型，Spring 会自动忽略 ThreadPoolTaskExecutor
    private final List<ExecutorService> executors;

    @PreDestroy
    public void shutdownAllExecutors() {
        log.info("Shutting down all executors {}-{}", executors.size(), executors);
        for (ExecutorService executor : executors) {
            try {
                executor.shutdown();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("All executors shut down.");
    }
}
