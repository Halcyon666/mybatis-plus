package com.whalefall541.cases.concurrentqry;

import com.whalefall541.mybatisplus.samples.generator.system.mapper.CodeEntityMapper;
import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.*;
import java.util.concurrent.*;

public class CodeQueryExecutor {

    private final SqlSessionFactory sqlSessionFactory;
    private final ExecutorService executor;

    public CodeQueryExecutor(SqlSessionFactory sqlSessionFactory, int threadPoolSize) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public Map<String, CodeEntityPO> queryUsers(Map<String, CodeEntityPO> userMap) throws ExecutionException {
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
                    // 包装异常，包含具体 userId
                    throw new CompletionException(new RuntimeException("查询失败 userId=" + userId, e));
                }
            }, executor);
            futures.add(future);
        }

        // 等待所有任务完成（失败也立即抛出）
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            allTasks.join(); // 会传播其中一个任务的异常
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            // 直接透传已包装过的异常
            throw new ExecutionException("并发查询失败：" + cause.getMessage(), cause);
        }

        return resultMap;
    }


    public void shutdown() {
        executor.shutdown();
    }
}
