# 多数据源动态切换使用指南

## 概述

本项目实现了多种方式来动态切换MyBatis-Plus中的数据源，支持在运行时根据业务需要选择不同的数据库连接。

## 实现方案

### 方案一：使用@DS注解（推荐）

这是最简单直接的方式，使用dynamic-datasource提供的@DS注解：

```java
@DS("master")
public List<CodeEntityPO> selectFromMaster() {
    return this.list();
}

@DS("slave")
public List<CodeEntityPO> selectFromSlave() {
    return this.list();
}
```

### 方案二：编程式动态切换

通过DataSourceContextHolder手动切换数据源：

```java
public List<CodeEntityPO> selectByDataSource(String dataSourceKey) {
    try {
        DataSourceContextHolder.setDataSource(dataSourceKey);
        return this.list();
    } finally {
        DataSourceContextHolder.clearDataSource();
    }
}
```

### 方案三：自定义注解+AOP切面

使用自定义的@DataSourceSwitcher注解：

```java
@DataSourceSwitcher(value = "master")
public List<CodeEntityPO> selectFromMasterWithAnnotation() {
    return this.list();
}

@DataSourceSwitcher(dataSource = DataSourceEnum.SLAVE)
public List<CodeEntityPO> selectFromSlaveWithEnum() {
    return this.list();
}
```

### 方案四：工具类方式（推荐）

使用DataSourceUtils工具类，提供最简洁的API：

```java
// 使用Lambda表达式在指定数据源中执行操作
public List<CodeEntityPO> selectFromMaster() {
    return DataSourceUtils.executeWithMaster(() -> this.list());
}

public List<CodeEntityPO> selectFromSlave() {
    return DataSourceUtils.executeWithSlave(() -> this.list());
}

// 动态指定数据源
public List<CodeEntityPO> selectByDataSource(String dataSourceKey) {
    return DataSourceUtils.executeWithDataSource(dataSourceKey, () -> this.list());
}

// 复杂业务操作
public String readWriteSeparation(String username) {
    // 从主库读取
    List<CodeEntityPO> data = DataSourceUtils.executeWithMaster(() ->
        this.lambdaQuery().eq(CodeEntityPO::getUsername, username).list()
    );

    // 保存到从库
    boolean result = DataSourceUtils.executeWithSlave(() ->
        this.save(processedData)
    );

    return "操作完成";
}
```

## 配置说明

### 1. 依赖配置

在build.gradle中添加：

```gradle
implementation 'com.baomidou:dynamic-datasource-spring-boot-starter:4.1.1'
implementation 'org.springframework.boot:spring-boot-starter-aop'
```

### 2. 数据源配置

在application.yml中配置多个数据源：

```yaml
spring:
  datasource:
    dynamic:
      primary: master # 默认数据源
      strict: false   # 是否严格匹配数据源
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

## API使用示例

### 基础用法

```bash
# 从主数据源查询
GET /system/codeEntity/master

# 从从数据源查询
GET /system/codeEntity/slave

# 动态指定数据源查询
GET /system/codeEntity/dynamic/master
GET /system/codeEntity/dynamic/slave
```

### 高级用法

```bash
# 使用注解方式查询
GET /system/advanced/master/annotation
GET /system/advanced/slave/annotation

# 使用枚举注解查询
GET /system/advanced/master/enum
GET /system/advanced/slave/enum

# 批量操作演示
POST /system/advanced/batch
```

### 工具类用法

```bash
# 使用工具类查询
GET /system/util/master
GET /system/util/slave

# 动态指定数据源
GET /system/util/dynamic/master
GET /system/util/dynamic/slave

# 使用枚举指定数据源
GET /system/util/enum/master
GET /system/util/enum/slave

# 读写分离示例
GET /system/util/read-write-separation/{username}

# 数据同步
POST /system/util/sync

# 数据对比
GET /system/util/compare
```

## 核心类说明

### 1. DataSourceEnum
数据源枚举类，定义可用的数据源：
- MASTER: 主数据源
- SLAVE: 从数据源

### 2. DataSourceContextHolder
数据源上下文持有者，用于ThreadLocal方式管理当前线程的数据源。

### 3. DataSourceSwitcher
自定义注解，支持通过注解方式声明数据源切换。

### 4. DataSourceAspect
AOP切面类，拦截@DataSourceSwitcher注解，实现自动数据源切换。

### 5. DataSourceUtils
工具类，提供便捷的数据源切换方法：
- `executeWithDataSource()`: 在指定数据源中执行操作
- `executeWithMaster()`: 在主数据源中执行操作
- `executeWithSlave()`: 在从数据源中执行操作
- 支持有返回值和无返回值的操作

## 最佳实践

### 1. 事务管理
- 确保数据源切换在事务开始之前完成
- AOP切面的@Order(1)确保在事务注解之前执行

### 2. 异常处理
- 使用try-finally确保数据源上下文被正确清理
- 避免数据源泄露到其他线程

### 3. 性能考虑
- 优先使用@DS注解，性能最好
- 工具类方式代码最简洁，推荐日常使用
- 编程式切换适合复杂业务逻辑
- 自定义注解适合统一管理和扩展

### 4. 方案选择建议
- **简单场景**: 使用@DS注解
- **复杂业务**: 使用DataSourceUtils工具类
- **统一管理**: 使用自定义@DataSourceSwitcher注解
- **特殊需求**: 使用编程式DataSourceContextHolder

## 注意事项

1. **线程安全**: DataSourceContextHolder使用ThreadLocal，确保线程安全
2. **资源清理**: 必须在finally块中清理数据源上下文
3. **事务边界**: 数据源切换必须在事务开始前完成
4. **配置验证**: 确保配置的数据源名称与代码中使用的一致

## 扩展建议

1. 可以根据用户ID、租户ID等业务参数动态选择数据源
2. 可以实现读写分离，查询操作使用从库，写操作使用主库
3. 可以添加数据源健康检查和故障转移机制
