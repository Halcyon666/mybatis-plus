package com.whalefall541.cases.concurrentqry;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 逻辑快速失败
 */
@Getter
@Slf4j
public class LogicalFailFastTaskExecutor implements AutoCloseable {

    private final ExecutorService executor;

    public LogicalFailFastTaskExecutor(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    public static void shutdown(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 失败日志会打印出 threadCount 条
     *
     * @param inputs       输入List P
     * @param taskFunction 执行函数
     * @param <P>          执行函数 入参
     * @param <R>          执行函数 出参
     * @return CompletableFuture<List < R>>
     */
    public <P, R> CompletableFuture<List<R>> executeAsyncTasks(List<P> inputs, Function<P, R> taskFunction) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        List<CompletableFuture<R>> futures = inputs.stream()
                .map(input -> CompletableFuture.supplyAsync(() -> {
                    if (atomicBoolean.get()) {
                        return null;
                    }
                    try {
                        return taskFunction.apply(input);
                    } catch (Exception e) {
                        log.error("任务处理失败: {}", input, e);
                        // fail fast
                        atomicBoolean.set(true);
                        throw new CompletionException(e);
                    }
                }, executor))
                .collect(Collectors.toList());

        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        futures.stream()
                                .map(CompletableFuture::join)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
    }

    @Override
    public void close() {
        shutdown(executor);
    }
}
