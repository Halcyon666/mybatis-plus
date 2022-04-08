package com.whalefall541.util;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import org.springframework.context.annotation.Bean;

public class MyEncryptablePropertyResolver implements EncryptablePropertyResolver {
    public MyEncryptablePropertyResolver() {
        super();
    }

    @Bean(name = "encryptablePropertyResolver")
    public EncryptablePropertyResolver encryptablePropertyResolver() {
        return new MyEncryptablePropertyResolver();
    }
    //自定义解密方法
    @Override
    public String resolvePropertyValue(String s) {
        if (null != s && s.startsWith(MyEncryptablePropertyDetector.encodePasswordPrefix)) {
            return EncryptUtil.decrypt(s.substring(MyEncryptablePropertyDetector.encodePasswordPrefix.length()));
        }
        return s;
    }
}