package com.whalefall541.cases.resilience4j;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Halcyon
 * @since 2025/7/25 22:23
 */
@Component
@Slf4j
public class ResilienceService {
    @RateLimiter(name = "#a0", fallbackMethod = "fallback")
    public String process(String limiterName) {
        return "Success from limiter: " + limiterName;
    }

    @SuppressWarnings("unused")
    public String fallback(String limiterName, RequestNotPermitted ex) {
        log.error("限流触发{}", ex.getMessage(), ex);

        return "限流触发 for limiter: " + limiterName;
    }


}
