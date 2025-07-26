package com.whalefall541.cases.concurrentqry.jobversion;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;


@SuppressWarnings("all")
@Slf4j
public class JobFailFastAsyncExecutor implements AutoCloseable {

    private final ExecutorService executor;

    public JobFailFastAsyncExecutor(int threadCount, String jobName) {
        this.executor = Executors.newFixedThreadPool(threadCount, r -> {
            Thread thread = new Thread(r);
            thread.setName(String.format("%s-%s", jobName, thread.getName()));
            return thread;
        });
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
                .map(input -> CompletableFuture.supplyAsync(
                        // 异步线程（来自 thread pool）执行下面逻辑
                        () -> taskFunction.apply(input), executor))
                .collect(Collectors.toList());
        CompletableFuture<List<R>> resultFuture = new CompletableFuture<>();
        CommonTaskSupport.registerFailFastHandlers(futures, resultFuture);
        CommonTaskSupport.collectAllResults(futures, resultFuture);
        return resultFuture;
    }

    static class CommonTaskSupport {
        private CommonTaskSupport() {
        }

        public static <R> void registerFailFastHandlers(List<CompletableFuture<R>> futures,
                                                        CompletableFuture<List<R>> resultFuture) {
            AtomicBoolean failFastTriggered = new AtomicBoolean(false);
            futures.forEach(future -> future.whenComplete(
                    // 下面都是的异步线程完成后触发
                    (r, ex) -> {
                        if (ex != null && failFastTriggered.compareAndSet(false, true)) {
                            Throwable actual = unwrap(ex);
                            logIfNeeded(actual);
                            resultFuture.completeExceptionally(actual);
                            futures.forEach(f -> {
                                boolean cancelled = f.cancel(true);
                                log.debug("尝试取消任务{}: {}", f, cancelled ? "成功" : "失败");
                            });
                        }
                    }));
        }

        private static Throwable unwrap(Throwable ex) {
            if (ex instanceof CompletionException || ex instanceof ExecutionException) {
                return ex.getCause();
            }
            return ex;
        }

        /**
         * 优雅关闭线程池
         *
         * @param executor 线程池
         */
        public static void shutdownGracefully(ExecutorService executor) {
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

        public static <R> void collectAllResults(List<CompletableFuture<R>> futures,
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


    @Override
    public void close() {
        CommonTaskSupport.shutdownGracefully(executor);
    }
}
