package com.whalefall541.cases.concurrentqry.v5;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**

 */
@Configuration
public class AsyncThreadPoolConfig {

    // 支持@Async
    @Bean(name = "sharedAsyncExecutor")
    public Executor sharedAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int core = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(core * 2);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("shared-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        executor.setAllowCoreThreadTimeOut(true);
        return executor;
    }

    // JDK原生线程池
    @Bean(name = "globalExecutor")
    public ExecutorService globalExecutor() {
        int core = Runtime.getRuntime().availableProcessors();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                core,                // corePoolSize
                core * 2,            // maximumPoolSize
                60L,                 // keepAliveTime
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory() {
                    private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
                    private final AtomicInteger count = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = defaultFactory.newThread(r);
                        thread.setName("global-executor-" + (count.incrementAndGet()));
                        // 设置	结果
                        // t.setDaemon(false);	线程是非守护线程，保持 JVM 活跃
                        // t.setDaemon(true);	线程是守护线程，JVM 退出时会被杀死
                        thread.setDaemon(false);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：主线程执行
        );
        // 对高并发环境，避免长时间空闲线程占用系统资源，保持轻量。
        // 核心线程也会跟非核心线程一样，空闲超过 keepAliveTime 后被自动销毁
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }


}
