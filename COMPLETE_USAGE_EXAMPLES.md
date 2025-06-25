# 完整的多数据源使用示例

## 🎯 解决方案总览

针对您的需求，我提供了4种数据源切换方案：

### 1. @DS注解方式（最简单）
```java
@DS("master")
public List<CodeEntityPO> selectFromMaster() {
    return this.list();
}
```

### 2. 手动切换方式（您图片中的用法）
```java
public boolean saveUser(CodeEntityPO entity) {
    DataSourceContextHolder.setDataSource("master");
    try {
        return this.save(entity);
    } finally {
        DataSourceContextHolder.clearDataSource();
    }
}
```

### 3. 工具类方式（推荐）
```java
public List<CodeEntityPO> selectFromMaster() {
    return DataSourceUtils.executeWithMaster(() -> this.list());
}
```

### 4. 自定义注解+AOP方式
```java
@DataSourceSwitcher(value = "master")
public List<CodeEntityPO> selectFromMaster() {
    return this.list();
}
```

## 🔧 如果@DS注解报红的解决方案

### 方案A：修复依赖问题
1. 确保build.gradle中有正确的依赖：
```gradle
implementation 'com.baomidou:dynamic-datasource-spring-boot-starter:3.5.2'
```

2. 刷新项目依赖：
```bash
./gradlew clean build --refresh-dependencies
```

### 方案B：使用手动切换（立即可用）
如果依赖问题暂时无法解决，直接使用手动切换：

```java
@Service
public class CodeEntityServiceImpl extends ServiceImpl<CodeEntityMapper, CodeEntityPO> {

    // 手动切换到主库保存
    public boolean saveToMaster(CodeEntityPO entity) {
        DataSourceContextHolder.setDataSource("master");
        try {
            return this.save(entity);
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }
    
    // 手动切换到从库查询
    public List<CodeEntityPO> selectFromSlave() {
        DataSourceContextHolder.setDataSource("slave");
        try {
            return this.list();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }
    
    // 动态选择数据源
    public List<CodeEntityPO> selectByDataSource(String dataSource) {
        DataSourceContextHolder.setDataSource(dataSource);
        try {
            return this.list();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }
}
```

### 方案C：使用工具类（最推荐）
```java
@Service
public class CodeEntityServiceImpl extends ServiceImpl<CodeEntityMapper, CodeEntityPO> {

    public boolean saveToMaster(CodeEntityPO entity) {
        return DataSourceUtils.executeWithMaster(() -> this.save(entity));
    }
    
    public List<CodeEntityPO> selectFromSlave() {
        return DataSourceUtils.executeWithSlave(() -> this.list());
    }
    
    public List<CodeEntityPO> selectByDataSource(String dataSource) {
        return DataSourceUtils.executeWithDataSource(dataSource, () -> this.list());
    }
    
    // 复杂业务操作
    public String complexOperation(CodeEntityPO entity) {
        // 从主库查询
        List<CodeEntityPO> masterData = DataSourceUtils.executeWithMaster(() -> 
            this.lambdaQuery().eq(CodeEntityPO::getUsername, entity.getUsername()).list()
        );
        
        // 保存到从库
        boolean saveResult = DataSourceUtils.executeWithSlave(() -> {
            entity.setCode(entity.getCode() + "_PROCESSED");
            return this.save(entity);
        });
        
        return String.format("主库查询: %d条, 从库保存: %s", 
                masterData.size(), saveResult ? "成功" : "失败");
    }
}
```

## 🚀 API接口示例

### Controller层使用
```java
@RestController
@RequestMapping("/api/codeEntity")
public class CodeEntityController {

    @Autowired
    private CodeEntityServiceImpl codeEntityService;
    
    @GetMapping("/master")
    public List<CodeEntityPO> selectFromMaster() {
        return codeEntityService.selectFromMaster();
    }
    
    @GetMapping("/slave")
    public List<CodeEntityPO> selectFromSlave() {
        return codeEntityService.selectFromSlave();
    }
    
    @GetMapping("/dynamic/{dataSource}")
    public List<CodeEntityPO> selectByDataSource(@PathVariable String dataSource) {
        return codeEntityService.selectByDataSource(dataSource);
    }
    
    @PostMapping("/save/{dataSource}")
    public boolean saveByDataSource(@RequestBody CodeEntityPO entity, @PathVariable String dataSource) {
        return DataSourceUtils.executeWithDataSource(dataSource, () -> 
            codeEntityService.save(entity)
        );
    }
}
```

## 📋 测试用例

```java
@SpringBootTest
public class DataSourceTest {

    @Autowired
    private CodeEntityServiceImpl codeEntityService;

    @Test
    public void testDataSourceSwitch() {
        // 测试主库操作
        List<CodeEntityPO> masterData = codeEntityService.selectFromMaster();
        System.out.println("主库数据量: " + masterData.size());
        
        // 测试从库操作
        List<CodeEntityPO> slaveData = codeEntityService.selectFromSlave();
        System.out.println("从库数据量: " + slaveData.size());
        
        // 测试动态切换
        List<CodeEntityPO> dynamicData = codeEntityService.selectByDataSource("master");
        System.out.println("动态切换数据量: " + dynamicData.size());
    }
}
```

## ⚡ 立即可用的完整代码

如果您想立即使用，只需要：

1. **复制核心类**：
   - `DataSourceContextHolder.java`
   - `DataSourceUtils.java`
   - `DataSourceEnum.java`

2. **修改您的Service**：
```java
// 在您现有的CodeEntityServiceImpl中添加方法
public List<CodeEntityPO> selectFromMasterManual() {
    DataSourceContextHolder.setDataSource("master");
    try {
        return this.list();
    } finally {
        DataSourceContextHolder.clearDataSource();
    }
}

// 或者使用工具类方式
public List<CodeEntityPO> selectFromMasterUtil() {
    return DataSourceUtils.executeWithMaster(() -> this.list());
}
```

3. **配置数据源**（application.yml）：
```yaml
spring:
  datasource:
    dynamic:
      primary: master
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

## 🎉 总结

- **最简单**: 直接使用手动切换方式（类似您图片中的用法）
- **最优雅**: 使用DataSourceUtils工具类
- **最灵活**: 结合多种方式使用
- **最稳定**: 确保每次操作后都清理数据源上下文

选择适合您项目的方式即可！
