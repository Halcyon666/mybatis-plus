package com.whalefall541.cases.concurrentqry.v1;

import com.whalefall541.mybatisplus.samples.generator.system.mapper.CodeEntityMapper;
import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import lombok.Getter;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * v1 直接把行为封装到工具类里面<br/>
 * 一旦某个线程抛出异常，其它未完成的任务基本就不会继续（因为异常在 runAsync 中传播得更快），所以只打印一个失败日志。
 */
public class CodeQueryExecutor {

    private final SqlSessionFactory sqlSessionFactory;
    @Getter
    private final ExecutorService executor;

    public CodeQueryExecutor(SqlSessionFactory sqlSessionFactory, int threadPoolSize) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public CompletableFuture<Map<String, CodeEntityPO>> queryUsersAsync(Map<String, CodeEntityPO> userMap) {
        Map<String, CodeEntityPO> resultMap = Collections.synchronizedMap(new HashMap<>());
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String userId : userMap.keySet()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try (SqlSession session = sqlSessionFactory.openSession()) {
                    CodeEntityMapper mapper = session.getMapper(CodeEntityMapper.class);
                    CodeEntityPO user = mapper.selectById(userId);
                    if (user != null) {
                        resultMap.put(userId, user);
                    }
                } catch (Exception e) {
                    // 包装为 CompletionException，让链可感知异常
                    throw new CompletionException("查询失败 userId=" + userId, e);
                }
            }, executor);
            futures.add(future);
        }

        // allOf 返回一个 CompletableFuture<Void>
        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> resultMap);
    }

}
