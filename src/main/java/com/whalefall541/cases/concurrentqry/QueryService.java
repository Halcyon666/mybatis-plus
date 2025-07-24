package com.whalefall541.cases.concurrentqry;

import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import com.whalefall541.mybatisplus.samples.generator.system.service.impl.CodeEntityServiceImpl;
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

import static com.whalefall541.cases.concurrentqry.LogicalFailFastTaskExecutor.shutdown;

/**
 * @author Halcyon
 * @date 2025/7/22 23:01
 * @since 1.0.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class QueryService {
    static final List<String> LIST = Arrays.asList("JACK0",
            "JACK1",
            "JACK2",
            "JACK3",
            "JACK4",
            "JACK5",
            "JACK6");
    static final String LOG_PATTERN = "{}: {}";
    private final SqlSessionFactory sqlSessionFactory;
    private CodeEntityServiceImpl codeEntityService;
    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> doCodeQuery();
    }

    public void doCodeQuery() {
        try {
            codeQuery2();
        } catch (Exception e) {
            log.error("并发查询code失败", e);
        }
    }

    @SuppressWarnings("unused")
    public void codeQuery() {
        CodeQueryExecutor queryExecutor = new CodeQueryExecutor(sqlSessionFactory, 3);

        Map<String, CodeEntityPO> userMap = getSourceMap();

        try {
            queryExecutor.queryUsersAsync(userMap)
                    .handle((resultMap, ex) -> {
                        if (ex != null) {
                            throw new CompletionException(ex);
                        }
                        resultMap.forEach((id, user) -> log.info(LOG_PATTERN, id, user));
                        return resultMap;
                        // .join() 等待任务完成，异常抛出
                        // 如果不调用，线程池会提前关闭，导致 com.whalefall541.cases.concurrentqry.QueryService.doCodeQuery
                        // 捕获不到异常
                    }).join();

        } finally {
            shutdown(queryExecutor.getExecutor());
        }
    }

    private static Map<String, CodeEntityPO> getSourceMap() {
        return LIST.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        obj -> new CodeEntityPO() {
                        }
                ));
    }

    private static List<CodeEntityPO> getPos() {
         return LIST.stream()
                .map(s -> {
                    CodeEntityPO codeEntityPO = new CodeEntityPO();
                    codeEntityPO.setUsername(s);
                    return codeEntityPO;
                }).collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public void codeQuery1() {
        List<CodeEntityPO> poList = getPos();
        try (LogicalFailFastTaskExecutor asyncTaskExecutor = new LogicalFailFastTaskExecutor(3)) {
            asyncTaskExecutor.executeAsyncTasks(poList, po -> codeEntityService.getById(po.getUsername()))
                    .handle((list, ex) -> {
                        if (ex != null) {
                            throw new CompletionException(ex);
                        }
                        log.info(LOG_PATTERN, list.size(), list);
                        return list;
                        // .join() 等待任务完成，异常抛出
                        // 如果不调用，线程池会提前关闭，导致 com.whalefall541.cases.concurrentqry.QueryService.doCodeQuery
                        // 捕获不到异常
                    }).join();
        }
    }

    public void codeQuery2() {
        List<CodeEntityPO> poList = getPos();
        try (FailFastAsyncExecutor asyncTaskExecutor = new FailFastAsyncExecutor(3)) {
            asyncTaskExecutor.executeFailFast(poList, po -> codeEntityService.getById(po.getUsername()))
                    .handle((list, ex) -> {
                        if (ex != null) {
                            throw new CompletionException(ex);
                        }
                        log.info(LOG_PATTERN, list.size(), list);
                        return list;
                        // .join() 等待任务完成，异常抛出
                        // 如果不调用，线程池会提前关闭，导致 com.whalefall541.cases.concurrentqry.QueryService.doCodeQuery
                        // 捕获不到异常
                    }).join();
        }
    }

}
