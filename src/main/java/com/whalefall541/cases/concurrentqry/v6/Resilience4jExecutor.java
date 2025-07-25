package com.whalefall541.cases.concurrentqry.v6;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
public class Resilience4jExecutor {

    private final ExecutorService executor = Executors.newFixedThreadPool(20);

    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;

    public Resilience4jExecutor(RetryRegistry retryRegistry,
                                CircuitBreakerRegistry circuitBreakerRegistry,
                                RateLimiterRegistry rateLimiterRegistry) {
        this.retryRegistry = retryRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    /*
        @RateLimiter 最先生效（限流）

        @CircuitBreaker 接着判断熔断

        @Retry 最后决定是否重试
     */
    @SuppressWarnings("all")
    public <P, R> CompletableFuture<List<R>> execute(List<P> inputs,
                                                     Function<P, R> taskFunction,
                                                     String retryName,
                                                     String cbName,
                                                     String limiterName) {
        Retry retry = retryRegistry.retry(retryName);
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(cbName);
        RateLimiter limiter = rateLimiterRegistry.rateLimiter(limiterName);

        List<CompletableFuture<R>> futures = inputs.stream().map(input -> {
            Supplier<R> decoratedSupplier = RateLimiter.decorateSupplier(limiter,
                    CircuitBreaker.decorateSupplier(cb,
                            Retry.decorateSupplier(retry, () -> taskFunction.apply(input))));

            return CompletableFuture.supplyAsync(decoratedSupplier, executor);
        }).collect(Collectors.toList());

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
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()))
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
}
