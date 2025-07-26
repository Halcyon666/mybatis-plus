package com.whalefall541.cases.resilience4j;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

/**
 * @author Halcyon
 * @since 2025/7/25 22:30
 */
@Service
@AllArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class CallService {
    private final ResilienceService resilienceService;
//    @Bean
    public CommandLineRunner commandLineRunnerResilience() {
        return args -> IntStream.range(0, 3)
                .mapToObj(i -> resilienceService.process("testLimiter"))
                .forEach(log::info);
    }

}
