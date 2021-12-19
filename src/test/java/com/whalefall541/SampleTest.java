package com.whalefall541;

import com.whalefall541.entity.Connect;
import com.whalefall541.entity.table.Users;
import com.whalefall541.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

/**
 * @author: WhaleFall541
 * @date: 2021/12/7 0:38
 * codes from https://mp.baomidou.com/guide/quick-start.html#%E5%BC%80%E5%A7%8B%E4%BD%BF%E7%94%A8
 */
@SpringBootTest(classes = App.class)
public class SampleTest implements ApplicationContextAware {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        List<Users> userList = userMapper.selectList(null);
        // Assert.isTrue(userList.size()==5,"结果集不为5条");
        userList.forEach(System.out::println);
    }

    @Test
    public void testDecrypt() {
        Connect bean = context.getBean(Connect.class);
        System.err.println(bean.getUsr() + " | " + bean.getPwd());
    }

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
