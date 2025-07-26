package com.whalefall541.cases.concurrentqry.v4;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.whalefall541.cases.concurrentqry.common.CommonTaskSupport.collectAllResults;
import static com.whalefall541.cases.concurrentqry.common.CommonTaskSupport.registerFailFastHandlers;

/**
 * @see com.whalefall541.cases.concurrentqry.v3.FailFastAsyncExecutor
 * 改进点	说明
 * ✅ 线程中断检测	Thread.currentThread().isInterrupted() 在任务执行前/后检查中断，确保 cancel(true) 有效
 * ✅ 任务失败后日志提示	在任务失败时打印日志，便于诊断是哪一个触发了 fail-fast
 * ✅ 异常传播清晰化	CompletionException.getCause() 还原真正抛出的业务异常
 * ✅ 线程名标识	修改线程名便于在日志或调试时追踪
 */
@Slf4j
public class FailFastAsyncExecutorV7 implements AutoCloseable {

    private final ExecutorService executor;

    public FailFastAsyncExecutorV7(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    public <P, R> CompletableFuture<List<R>> executeFailFast(List<P> inputs, Function<P, R> taskFunction) {
        List<CompletableFuture<R>> futures = inputs.stream()
                .map(input -> wrapInterruptibleTask(input, taskFunction))
                .collect(Collectors.toList());

        CompletableFuture<List<R>> resultFuture = new CompletableFuture<>();
        registerFailFastHandlers(futures, resultFuture);
        collectAllResults(futures, resultFuture);

        return resultFuture;
    }


    /**
     * @see InterruptibleTaskWrapper
     */
    private <P, R> CompletableFuture<R> wrapInterruptibleTask(P input, Function<P, R> taskFunction) {
        return CompletableFuture.supplyAsync(() -> {
            Thread current = Thread.currentThread();
            String oldName = current.getName();
            if (!oldName.startsWith("FailFast-")) {
                current.setName("FailFast-" + oldName);
            }

            if (current.isInterrupted()) {
                log.debug("任务启动前已被中断，抛出异常终止任务");
                throw new CancellationException("Task was interrupted before start");
            }

            R result = taskFunction.apply(input);

            if (current.isInterrupted()) {
                log.debug("任务执行后检测到中断，结果作废");
                throw new CancellationException("Task was interrupted after execution");
            }

            return result;
        }, executor);
    }

    public static Throwable unwrap(Throwable ex) {
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

    @Override
    public void close() {
        shutdown(executor);
    }
}
