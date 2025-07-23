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
import java.util.concurrent.CompletionException;
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
        } catch (Exception e) {
            log.error("并发查询code失败", e);
        }
    }

    public void codeQuery() {
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
            executor.queryUsersAsync(userMap)
                    .handle((resultMap, ex) -> {
                        if (ex != null) {
                            throw new CompletionException(ex);
                        }
                        resultMap.forEach((id, user) -> log.info("{}: {}", id, user));
                        return resultMap;
                        // .join() 等待任务完成，异常抛出
                        // 如果不调用，线程池会提前关闭，导致 com.whalefall541.cases.concurrentqry.QueryService.doCodeQuery
                        // 捕获不到异常
                    }).join();

        } finally {
            executor.shutdown();
        }
    }
}
