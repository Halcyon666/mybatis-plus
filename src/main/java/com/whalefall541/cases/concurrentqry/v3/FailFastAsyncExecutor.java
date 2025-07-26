package com.whalefall541.cases.concurrentqry.v3;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.whalefall541.cases.concurrentqry.common.CommonTaskSupport.collectAllResults;
import static com.whalefall541.cases.concurrentqry.common.CommonTaskSupport.registerFailFastHandlers;
import static com.whalefall541.cases.concurrentqry.v2.LogicalFailFastTaskExecutor.shutdown;

/**
 * Cancel快速失败
 * Recommend version
 * <p>
 * 你的 FailFastAsyncExecutor 实现了 AutoCloseable，用完后关闭线程池是个好习惯，避免线程泄漏。
 * 如果是高并发频繁调用，考虑线程池复用或者改为共享线程池，避免频繁创建销毁。
 * <p>
 * 一个支持中断响应的任务包装，或者帮你写个完整示例演示异常传播和取消
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
        registerFailFastHandlers(futures, resultFuture);
        collectAllResults(futures, resultFuture);

        return resultFuture;
    }

    @Override
    public void close() {
        shutdown(executor);
    }
}
