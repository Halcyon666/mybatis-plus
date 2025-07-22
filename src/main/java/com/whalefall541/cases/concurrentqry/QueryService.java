package com.whalefall541.cases.concurrentqry;

import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Halcyon
 * @date 2025/7/22 23:01
 * @since 1.0.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class QueryService {
    private final SqlSessionFactory sqlSessionFactory;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> doCodeQuery();
    }

    public void doCodeQuery() {
        try {
            codeQuery();
        } catch (ExecutionException e) {
            log.error("并发查询code失败", e);
        }
    }

    public void codeQuery() throws ExecutionException {
        CodeQueryExecutor executor = new CodeQueryExecutor(sqlSessionFactory, 3);

        List<String> userIds = Arrays.asList("JACK0",
                "JACK1",
                "JACK2",
                "JACK3",
                "JACK4",
                "JACK5",
                "JACK6");
        Map<String, CodeEntityPO> userMap = userIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        obj -> new CodeEntityPO() {
                        }
                ));

        try {
            Map<String, CodeEntityPO> result = executor.queryUsers(userMap);
            result.forEach((id, user) -> log.info("{}: {}", id, user));
        } finally {
            executor.shutdown();
        }
    }
}
