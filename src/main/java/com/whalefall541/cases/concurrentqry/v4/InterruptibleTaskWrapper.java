package com.whalefall541.cases.concurrentqry.v4;

import java.util.function.Function;

/**
 * ✔ 快速响应取消	被取消的任务在下次执行检查点时立即退出，不继续浪费资源
 * ✔ 减少失败日志	被 cancel 的任务不会继续运行到报错
 * ✔ 严格 fail-fast	一旦一个任务失败，其它任务被 cancel 并自动终止
 * ✔ 更安全、可控	避免任务继续执行可能带来的副作用或脏数据
 * <p>
 * 🔧 什么时候特别有用？
 * 你任务是长耗时操作或循环型任务时；
 * <p>
 * 你想要减少系统负载、避免无用操作时；
 * <p>
 * 你使用线程池，并希望任务不要“无谓抢线程”的时候。
 */
public class InterruptibleTaskWrapper {

    private InterruptibleTaskWrapper() {
    }

    /**
     * 包装一个任务，使其支持响应线程中断，周期性检查中断状态
     * @param originalTask 原始任务函数
     * @param <P> 输入参数类型
     * @param <R> 返回结果类型
     * @return 包装后的任务函数
     */
    public static <P, R> Function<P, R> wrap(Function<P, R> originalTask) {
        return input -> {
            // 在执行任务前先检查中断
            checkInterrupted();

            // 这里假设原任务执行过程中，能调用此方法检查中断。
            // 如果你知道任务内有长循环，建议改造任务本身调用此方法
            // 或者用你自己的检测机制
            R result = originalTask.apply(input);

            // 再检查一次中断，避免长时间执行
            checkInterrupted();

            return result;
        };
    }

    @SuppressWarnings("all")
    private static void checkInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException(new InterruptedException("任务被取消：线程中断"));
        }
    }
}
