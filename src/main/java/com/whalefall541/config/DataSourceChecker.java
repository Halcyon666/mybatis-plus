package com.whalefall541.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

@Slf4j
@Component
public class DataSourceChecker implements CommandLineRunner {

    @Resource
    private List<DataSource> dataSourceList;

    @Override
    public void run(String... args) {
        dataSourceList.forEach(ds -> {
            // 在项目启动时“预热”连接池
            try (Connection conn = ds.getConnection()) {
                log.info("✅ 数据源连接成功：{}-{}", ds.getClass().getName(), conn);
            } catch (Exception e) {
                log.error("❌ 数据源连接失败：{}", e.getMessage());
            }
        });
    }
}
