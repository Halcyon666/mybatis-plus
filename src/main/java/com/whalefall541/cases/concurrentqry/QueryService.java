package com.whalefall541.cases.concurrentqry;

import com.whalefall541.cases.concurrentqry.v1.CodeQueryExecutor;
import com.whalefall541.cases.concurrentqry.v2.LogicalFailFastTaskExecutor;
import com.whalefall541.cases.concurrentqry.v3.FailFastAsyncExecutor;
import com.whalefall541.cases.concurrentqry.v4.InterruptibleTaskWrapper;
import com.whalefall541.cases.concurrentqry.v5.FailFastAsyncExecutorV5;
import com.whalefall541.cases.concurrentqry.v6.Resilience4jExecutor;
import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import com.whalefall541.mybatisplus.samples.generator.system.service.impl.CodeEntityServiceImpl;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.whalefall541.cases.concurrentqry.v2.LogicalFailFastTaskExecutor.shutdown;

/**
 * @author Halcyon
 * @date 2025/7/22 23:01
 * @since 1.0.0
 */
@Slf4j
@Service
@AllArgsConstructor
@SuppressWarnings("all")
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
    private final Executor globalExecutor;

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

//    @Bean
//    public CommandLineRunner commandLineRunner() {
//        return args -> doCodeQuery();
//    }


    public void doCodeQuery() {
        try {
            resilience4jCall();
        } catch (Exception e) {
            log.error("并发查询code失败", e);
        }
    }

    public void codeQuery1() {
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

    public void codeQuery2() {
        List<CodeEntityPO> poList = getPos();
        try (LogicalFailFastTaskExecutor asyncTaskExecutor = new LogicalFailFastTaskExecutor(3)) {
            asyncTaskExecutor.executeAsyncTasks(poList, po -> codeEntityService.getById(po.getUsername()))
                    .handle((list, ex) -> {
                        if (ex != null) {
                            throw new CompletionException(ex);
                        }
                        log.info(LOG_PATTERN, list.size(), list);
                        return list;
                    }).join();
        }
    }

    // real FailFast
    public void codeQuery3() {
        List<CodeEntityPO> poList = getPos();
        try (FailFastAsyncExecutor asyncTaskExecutor = new FailFastAsyncExecutor(3)) {
            asyncTaskExecutor.executeFailFast(poList, po -> codeEntityService.getById(po.getUsername()))
                    .handle((list, ex) -> {
                        if (ex != null) {
                            throw new CompletionException(ex);
                        }
                        log.info(LOG_PATTERN, list.size(), list);
                        return list;
                    }).join();
        }
    }


    // 响应中断
    public void codeQuery4() {
        // 如果你的 taskFunction 内部有循环或长耗时操作，最好把 checkInterrupted() 放在循环体里多次调用；
        Function<String, CodeEntityPO> interruptibleTask = InterruptibleTaskWrapper
                .wrap(s -> codeEntityService.getById(s));

        try (FailFastAsyncExecutor asyncTaskExecutor = new FailFastAsyncExecutor(3)) {
            asyncTaskExecutor.executeFailFast(LIST, interruptibleTask)
                    .handle((list, ex) -> {
                        if (ex != null) {
                            throw new CompletionException(ex);
                        }
                        log.info(LOG_PATTERN, list.size(), list);
                        return list;
                    }).join();
        }
    }

    // 使用共享线程池
    public void queryCodesWithFailFast5() {
        Function<String, CodeEntityPO> interruptibleTask = InterruptibleTaskWrapper
                .wrap(s -> codeEntityService.getById(s));

        FailFastAsyncExecutorV5 executor = new FailFastAsyncExecutorV5(globalExecutor);
        CompletableFuture<List<CodeEntityPO>> future = executor.executeFailFast(
                LIST,
                interruptibleTask
        );

        List<CodeEntityPO> results = future.join();
        log.info("查询完成，成功结果 {} 条", results.size());
    }


    public void codeQueryWithTransactional1() {
        List<CodeEntityPO> poList = getPos();
        try (FailFastAsyncExecutor asyncTaskExecutor = new FailFastAsyncExecutor(3)) {
            asyncTaskExecutor.executeFailFast(poList, po -> codeEntityService.getByIdMine(po.getUsername()))
                    .handle((list, ex) -> {
                        if (ex != null) {
                            throw new CompletionException(ex);
                        }
                        log.info(LOG_PATTERN, list.size(), list);
                        return list;
                    }).join();
        }
    }

    private final PlatformTransactionManager transactionManager;

    public void codeQueryWithTransactional2() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        FailFastAsyncExecutorV5 executor = new FailFastAsyncExecutorV5(globalExecutor);
        executor.executeFailFast(LIST, id -> template.execute(
                        status -> codeEntityService.getByIdMine(id)))
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("任务执行失败：{}", ex.getMessage(), ex);
                    } else {
                        extracted(result);
                    }
                });
    }

    private final Resilience4jExecutor resilience4jExecutor;

    // 正常版本
    public void resilience4jCall() {
        resilience4jExecutor.execute(LIST,
                id -> codeEntityService.getById(id),
                "remoteServiceRetry",
                "remoteServiceCB",
                "remoteServiceLimiter"
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("任务执行失败：{}", ex.getMessage(), ex);
            } else {
                extracted(result);
            }
        });

    }

    private static void extracted(List<CodeEntityPO> result) {
        log.info("任务成功结果: {}", result);
    }

    // 限流拒绝
    public void resilience4jCallRequestNotPermitted() {

        resilience4jExecutor.execute(
                LIST,
                id -> codeEntityService.getById(id),
                "remoteServiceRetry",
                "remoteServiceCB",
                "testLimiter"
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                Throwable cause = ex.getCause();
                if (cause instanceof RequestNotPermitted) {
                    log.warn("限流拒绝: {}", cause.getMessage());
                } else if (cause instanceof CallNotPermittedException) {
                    log.warn("熔断器打开: {}", cause.getMessage());
                } else {
                    log.error("任务异常", cause);
                }
            } else {
                log.info("执行成功: {}", result);
            }
        });
    }


}
