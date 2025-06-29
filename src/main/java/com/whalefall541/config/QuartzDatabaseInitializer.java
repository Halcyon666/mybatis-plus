package com.whalefall541.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Quartz数据库初始化器
 * 在应用启动时检查并创建Quartz所需的数据库表
 *
 * @author xx
 * @since 2024-07-10
 */
@Slf4j
@Component
@Order(0) // 确保在Quartz启动之前执行，使用更高的优先级
public class QuartzDatabaseInitializer implements CommandLineRunner {

    private final DataSource slaveDataSource;

    public QuartzDatabaseInitializer(@Qualifier("slaveDataSource") DataSource slaveDataSource) {
        this.slaveDataSource = slaveDataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("开始检查Quartz数据库表...");

        try (Connection connection = slaveDataSource.getConnection()) {
            // 检查QRTZ_LOCKS表是否存在
            if (!tableExists(connection)) {
                log.info("Quartz表不存在，开始创建...");
                createQuartzTables(connection);
                log.info("Quartz表创建完成！");
            } else {
                log.info("Quartz表已存在，跳过创建。");
            }
        } catch (Exception e) {
            log.error("初始化Quartz数据库表失败", e);
            throw e;
        }
    }

    /**
     * 检查表是否存在
     */
    private boolean tableExists(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT 1 FROM " + "QRTZ_LOCKS" + " LIMIT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建Quartz表
     */
    private void createQuartzTables(Connection connection) throws Exception {
        // 读取SQL脚本
        ClassPathResource resource = new ClassPathResource("sql/quartz_tables_mysql.sql");
        String sql = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

        // 分割SQL语句并执行
        String[] sqlStatements = sql.split(";");

        try (Statement statement = connection.createStatement()) {
            for (String sqlStatement : sqlStatements) {
                String trimmedSql = sqlStatement.trim();
                if (!trimmedSql.isEmpty() && !trimmedSql.startsWith("--") && !trimmedSql.startsWith("/*")) {
                    try {
                        statement.execute(trimmedSql);
                        log.debug("执行SQL: {}", trimmedSql.substring(0, Math.min(50, trimmedSql.length())) + "...");
                    } catch (Exception e) {
                        // 忽略一些可能的错误（如表已存在等）
                        if (!e.getMessage().contains("already exists") &&
                            !e.getMessage().contains("Duplicate entry")) {
                            log.warn("执行SQL时出现警告: {} - SQL: {}", e.getMessage(), trimmedSql);
                        }
                    }
                }
            }
        }
    }
}
