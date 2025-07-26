package com.whalefall541.cases.concurrentqry.v4;

import com.whalefall541.cases.concurrentqry.common.CommonTaskSupport;
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
 * <p>
 * 实用建议
 * 重点关注 fail-fast 的核心价值：
 * <p>
 * ✅ 避免启动新任务：这是最重要的，也是最容易实现的
 * ✅ 快速失败反馈：一旦有任务失败，立即停止整个流程
 * ❓ 中断正在执行的任务：这个在实际项目中很少需要，实现复杂且收益有限
 * <p>
 * 什么时候需要中断响应：
 * <p>
 * 任务执行时间很长（比如几分钟、几小时）
 * 任务会消耗大量资源
 * 任务有明确的检查点可以安全中断
 * <p>
 * 大多数情况下：
 * <p>
 * 让正在执行的任务自然完成
 * 重点确保不启动新的任务
 * 这样既简单又足够
 */
@Slf4j
public class FailFastAsyncExecutorV7 implements AutoCloseable {

    private final ExecutorService executor;

    public FailFastAsyncExecutorV7(int threadCount, String jobName) {
        this.executor = Executors.newFixedThreadPool(threadCount, r -> {
            Thread thread = new Thread(r);
            thread.setName(String.format("%s-%s", jobName, thread.getName()));
            return thread;
        });
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


    // 业务方法中怎么设置，其实wrappertask 不实用，每个业务是否可中断 ，或者中断点不一样。
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



    @Override
    public void close() {
        CommonTaskSupport.shutdown(executor);
    }
}
