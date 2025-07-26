package com.whalefall541.cases.concurrentqry.common;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Halcyon
 * @since 2025/7/26 17:11
 */
@Slf4j
public class CommonTaskSupport {
    private CommonTaskSupport() {
    }
    public static <R> void registerFailFastHandlers(List<CompletableFuture<R>> futures,
                                                    CompletableFuture<List<R>> resultFuture) {
        AtomicBoolean failFastTriggered = new AtomicBoolean(false);
        futures.forEach(future -> future.whenComplete((r, ex) -> {
            if (ex != null && failFastTriggered.compareAndSet(false, true)) {
                Throwable actual = unwrap(ex);
                logIfNeeded(actual);
                resultFuture.completeExceptionally(actual);
                futures.forEach(f -> f.cancel(true));
            }
        }));
    }

    private static Throwable unwrap(Throwable ex) {
        if (ex instanceof CompletionException || ex instanceof ExecutionException) {
            return ex.getCause();
        }
        return ex;
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

    private static void logIfNeeded(Throwable actual) {
        if (!(actual instanceof CancellationException)) {
            log.warn("任务失败，开始 fail-fast 取消其他任务：{} - [{}]",
                    actual != null ? actual.getMessage() : "null",
                    actual != null ? actual.getClass().getSimpleName() : "null");
        }
    }

    public static  <R> void collectAllResults(List<CompletableFuture<R>> futures,
                                              CompletableFuture<List<R>> resultFuture) {
        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((v, ex) -> {
                    if (!resultFuture.isDone()) {
                        try {
                            List<R> results = futures.stream()
                                    .map(CompletableFuture::join)
                                    .collect(Collectors.toList());
                            resultFuture.complete(results);
                        } catch (CompletionException e) {
                            resultFuture.completeExceptionally(e.getCause());
                        }
                    }
                });
    }
}
