package com.whalefall541.cases.concurrentqry;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 方法	说明	示例
 * 1. Thread.sleep() 等	被调用时会抛出 InterruptedException，自动退出	Thread.sleep(1000)
 * 2. Thread.currentThread().isInterrupted()	主动轮询中断标志，需要自行退出循环	常用于非阻塞任务（如纯 CPU 循环）
 * 3. 阻塞方法（如队列、锁）	如 BlockingQueue.take()、Lock.lockInterruptibly()	支持中断后抛出异常退出
 */
@Slf4j
@SuppressWarnings("all")
public class CancelInterruptDemo {
    public static void main(String[] args) throws InterruptedException {
        test2();
    }

    private static void test0() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> future = executor.submit(() -> {
            try {
                while (true) {
                    log.info("任务运行中...");
                    Thread.sleep(1000); // 可中断方法
                }
            } catch (InterruptedException e) {
                log.info("任务收到中断信号，退出");
                Thread.currentThread().interrupt(); // 恢复中断状态（惯例）
            }
        });

        // 等待 2 秒后取消任务
        Thread.sleep(2000);
        log.info("尝试取消任务...");
        future.cancel(true); // 向任务线程发出中断

        executor.shutdown();
    }

    private static void test2() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> future = executor.submit(() -> {
            while (true) {
                // 主动检查中断标志
                if (Thread.currentThread().isInterrupted()) {
                    log.info("任务检测到中断信号，退出");
                    break;
                }
                // 模拟工作（不阻塞，仅耗 CPU）
                performCpuWork();
            }
        });

        // 等待 2 秒后取消任务
        Thread.sleep(2000);
        log.info("尝试取消任务...");
        future.cancel(true); // 向任务线程发出中断

        executor.shutdown();
    }

    private static void performCpuWork() {
        // 做一点轻量工作，避免死循环占满 CPU
        for (int i = 0; i < 1000000; i++) {
            Math.pow(i, 0.5); // 模拟计算
        }
    }


    private static void test1() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    log.info("线程工作中...");
                    Thread.sleep(1000); // 阻塞点，可被中断
                }
            } catch (InterruptedException e) {
                log.info("线程被中断，准备退出...");
                Thread.currentThread().interrupt(); // 可选：恢复中断状态
            }
        });

        thread.start();

        // 主线程等待 3 秒后中断子线程
        Thread.sleep(3000);
        log.info("主线程中断子线程...");
        thread.interrupt(); // 主动发起中断
    }
}
