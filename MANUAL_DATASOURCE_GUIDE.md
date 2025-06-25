# 手动数据源切换完整指南

## 问题解决

### 1. @DS注解报红问题

如果`@DS`注解报红，请确保：

1. **添加正确的依赖**（在build.gradle中）：
```gradle
// 对于Spring Boot 2.6.x，使用兼容版本
implementation 'com.baomidou:dynamic-datasource-spring-boot-starter:3.5.2'

// 对于Spring Boot 2.7+，可以使用更新版本
// implementation 'com.baomidou:dynamic-datasource-spring-boot-starter:4.1.1'
```

2. **正确的导入语句**：
```java
import com.baomidou.dynamic.datasource.annotation.DS;
```

3. **如果依然报红，可以暂时注释掉@DS注解，使用手动切换方式**

### 2. 手动数据源切换方案

#### 方案A：基础手动切换（推荐）

```java
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    public void saveUser(User user) {
        // 手动设置数据源
        DataSourceContextHolder.setDataSource("master");
        try {
            // 执行数据库操作
            userMapper.insert(user);
        } finally {
            // 清理数据源上下文
            DataSourceContextHolder.clearDataSource();
        }
    }
    
    public List<User> getUsersFromSlave() {
        DataSourceContextHolder.setDataSource("slave");
        try {
            return userMapper.selectList(null);
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }
}
```

#### 方案B：工具类封装（最推荐）

```java
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    public void saveUser(User user) {
        // 使用工具类，自动管理数据源上下文
        DataSourceUtils.executeWithMaster(() -> {
            userMapper.insert(user);
        });
    }
    
    public List<User> getUsersFromSlave() {
        return DataSourceUtils.executeWithSlave(() -> {
            return userMapper.selectList(null);
        });
    }
    
    public List<User> getUsersByDataSource(String dataSource) {
        return DataSourceUtils.executeWithDataSource(dataSource, () -> {
            return userMapper.selectList(null);
        });
    }
}
```

#### 方案C：AOP自动管理

如果您希望AOP自动清理数据源上下文，可以这样写：

```java
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    // AOP会自动清理数据源上下文
    public List<User> getUsers(String dataSource) {
        DataSourceContextHolder.setDataSource(dataSource);
        return userMapper.selectList(null);
    }
}
```

## 完整实现代码

### 1. DataSourceContextHolder.java
```java
public class DataSourceContextHolder {
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
    
    public static void setDataSource(String dataSourceKey) {
        CONTEXT_HOLDER.set(dataSourceKey);
    }
    
    public static String getDataSource() {
        return CONTEXT_HOLDER.get();
    }
    
    public static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }
}
```

### 2. DataSourceUtils.java（工具类）
```java
public class DataSourceUtils {
    
    public static <T> T executeWithDataSource(String dataSourceKey, Supplier<T> supplier) {
        try {
            DataSourceContextHolder.setDataSource(dataSourceKey);
            return supplier.get();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }
    
    public static <T> T executeWithMaster(Supplier<T> supplier) {
        return executeWithDataSource("master", supplier);
    }
    
    public static <T> T executeWithSlave(Supplier<T> supplier) {
        return executeWithDataSource("slave", supplier);
    }
    
    public static void executeWithDataSource(String dataSourceKey, Runnable runnable) {
        try {
            DataSourceContextHolder.setDataSource(dataSourceKey);
            runnable.run();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }
}
```

### 3. 配置文件 application.yml
```yaml
spring:
  datasource:
    dynamic:
      primary: master
      strict: false
      datasource:
        master:
          url: jdbc:oracle:thin:@192.168.3.161:1521:jc
          username: jc
          password: 123456
          driver-class-name: oracle.jdbc.driver.OracleDriver
        slave:
          url: jdbc:oracle:thin:@192.168.3.162:1521:jc2
          username: jc2
          password: 123456
          driver-class-name: oracle.jdbc.driver.OracleDriver
```

## 使用示例

### 基础CRUD操作

```java
@Service
public class CodeEntityServiceImpl extends ServiceImpl<CodeEntityMapper, CodeEntityPO> {

    // 保存到主库
    public boolean saveToMaster(CodeEntityPO entity) {
        return DataSourceUtils.executeWithMaster(() -> this.save(entity));
    }
    
    // 从从库查询
    public List<CodeEntityPO> selectFromSlave() {
        return DataSourceUtils.executeWithSlave(() -> this.list());
    }
    
    // 动态选择数据源
    public List<CodeEntityPO> selectByDataSource(String dataSource) {
        return DataSourceUtils.executeWithDataSource(dataSource, () -> this.list());
    }
}
```

### 复杂业务操作

```java
public String complexOperation(CodeEntityPO entity) {
    // 从主库查询
    List<CodeEntityPO> masterData = DataSourceUtils.executeWithMaster(() -> {
        return this.lambdaQuery()
                .eq(CodeEntityPO::getUsername, entity.getUsername())
                .list();
    });
    
    // 保存到从库
    boolean saveResult = DataSourceUtils.executeWithSlave(() -> {
        entity.setCode(entity.getCode() + "_PROCESSED");
        return this.save(entity);
    });
    
    return String.format("主库查询: %d条, 从库保存: %s", 
            masterData.size(), saveResult ? "成功" : "失败");
}
```

## 注意事项

1. **线程安全**: DataSourceContextHolder使用ThreadLocal，确保线程安全
2. **资源清理**: 必须在finally块中清理数据源上下文，避免内存泄露
3. **事务边界**: 数据源切换必须在事务开始前完成
4. **异常处理**: 确保在异常情况下也能正确清理资源

## 故障排除

1. **@DS注解不生效**: 检查依赖版本是否与Spring Boot版本兼容
2. **数据源切换失败**: 确保配置文件中的数据源名称与代码中使用的一致
3. **内存泄露**: 确保每次设置数据源后都有对应的清理操作
4. **事务问题**: 确保数据源切换在@Transactional注解之前生效

## 推荐使用顺序

1. **首选**: DataSourceUtils工具类方式
2. **备选**: 手动try-finally方式  
3. **高级**: 自定义注解+AOP方式
4. **简单**: @DS注解方式（如果依赖正常）
