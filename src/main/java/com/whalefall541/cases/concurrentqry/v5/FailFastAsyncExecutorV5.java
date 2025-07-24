package com.whalefall541.cases.concurrentqry.v5;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class FailFastAsyncExecutorV5 {

    private final Executor executor;

    public FailFastAsyncExecutorV5(Executor executor) {
        this.executor = executor;
    }

    @SuppressWarnings("all")
    public <P, R> CompletableFuture<List<R>> executeFailFast(List<P> inputs, Function<P, R> taskFunction) {
        List<CompletableFuture<R>> futures = inputs.stream()
                .map(input -> CompletableFuture.supplyAsync(() -> taskFunction.apply(input), executor))
                .collect(Collectors.toList());

        CompletableFuture<List<R>> resultFuture = new CompletableFuture<>();

        for (CompletableFuture<R> future : futures) {
            future.whenComplete((r, ex) -> {
                if (ex != null) {
                    resultFuture.completeExceptionally(ex);
                    futures.forEach(f -> f.cancel(true));
                }
            });
        }

        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .filter(f -> !f.isCompletedExceptionally() && !f.isCancelled())
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                )
                .whenComplete((result, ex) -> {
                    if (!resultFuture.isDone()) {
                        if (ex != null) {
                            resultFuture.completeExceptionally(ex);
                        } else {
                            resultFuture.complete(result);
                        }
                    }
                });

        return resultFuture;
    }

}
