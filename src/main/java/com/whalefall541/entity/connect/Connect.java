package com.whalefall541.entity.connect;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class Connect {
    @Value("${spring.datasource.dynamic.datasource.master.password}")
    private String pwd;
    @Value("${spring.datasource.dynamic.datasource.master.username}")
    private String usr;
    @Value("${spring.datasource.dynamic.datasource.master.url}")
    private String url;
    @Value("${spring.datasource.dynamic.datasource.master.driver-class-name}")
    private String driverClassName;

    // 从数据源配置
    @Value("${spring.datasource.dynamic.datasource.slave.password}")
    private String slavePwd;
    @Value("${spring.datasource.dynamic.datasource.slave.username}")
    private String slaveUsr;
    @Value("${spring.datasource.dynamic.datasource.slave.url}")
    private String slaveUrl;
    @Value("${spring.datasource.dynamic.datasource.slave.driver-class-name}")
    private String slaveDriverClassName;

}
