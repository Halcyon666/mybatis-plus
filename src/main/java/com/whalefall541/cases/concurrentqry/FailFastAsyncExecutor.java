package com.whalefall541.cases.concurrentqry;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.whalefall541.cases.concurrentqry.LogicalFailFastTaskExecutor.shutdown;

/**
 * Cancel快速失败
 * Recommend version
 */
@Slf4j
public class FailFastAsyncExecutor implements AutoCloseable {

    private final ExecutorService executor;

    public FailFastAsyncExecutor(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    /**
     * 执行一组异步任务，一旦其中任一任务失败，立即取消所有任务，并传播异常 <br/>
     * 真正严格意义的“fail-fast + 最少日志
     *
     * @param inputs       输入参数列表
     * @param taskFunction 任务处理函数，输入 P 返回 R
     * @return 一个异步 CompletableFuture，成功返回结果列表，失败抛出第一个异常
     */
    public <P, R> CompletableFuture<List<R>> executeFailFast(List<P> inputs, Function<P, R> taskFunction) {
        List<CompletableFuture<R>> futures = inputs.stream()
                .map(input -> CompletableFuture.supplyAsync(() -> taskFunction.apply(input), executor))
                .collect(Collectors.toList());

        CompletableFuture<List<R>> resultFuture = new CompletableFuture<>();

        for (CompletableFuture<R> future : futures) {
            future.whenComplete((r, ex) -> {
                if (ex != null) {
                    // 传播异常
                    resultFuture.completeExceptionally(ex);
                    // 取消所有其他任务
                    futures.forEach(f -> f.cancel(true));
                }
            });
        }

        // 如果所有任务都成功，则组装结果返回
        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        futures.stream()
                                .map(CompletableFuture::join)
                                .collect(Collectors.toList())
                )
                .whenComplete((resultList, ex) -> {
                    if (!resultFuture.isDone()) {
                        if (ex != null) {
                            resultFuture.completeExceptionally(ex);
                        } else {
                            resultFuture.complete(resultList);
                        }
                    }
                });

        return resultFuture;
    }

    @Override
    public void close() {
        shutdown(executor);
    }
}
