package com.whalefall541.entity.connect;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class Connect {
    @Value("${spring.datasource.password}")
    private String pwd;
    @Value("${spring.datasource.username}")
    private String usr;

}
