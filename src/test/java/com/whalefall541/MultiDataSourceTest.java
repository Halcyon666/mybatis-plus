package com.whalefall541;

import com.whalefall541.mybatisplus.samples.generator.system.po.CodeEntityPO;
import com.whalefall541.mybatisplus.samples.generator.system.service.impl.CodeEntityServiceImpl;
import com.whalefall541.mybatisplus.samples.generator.system.service.impl.CodeEntityAdvancedServiceImpl;
import com.whalefall541.mybatisplus.samples.generator.system.service.impl.ManualDataSourceServiceImpl;
import com.whalefall541.config.DataSourceEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 多数据源测试类
 * 
 * @author xx
 * @since 2024-07-10
 */
@SpringBootTest(classes = App.class)
public class MultiDataSourceTest {

    @Autowired
    private CodeEntityServiceImpl codeEntityService;
    
    @Autowired
    private CodeEntityAdvancedServiceImpl advancedService;

    @Autowired
    private ManualDataSourceServiceImpl manualService;

    /**
     * 测试@DS注解方式
     */
    @Test
    public void testDSAnnotation() {
        System.out.println("=== 测试@DS注解方式 ===");
        
        // 从主数据源查询
        List<CodeEntityPO> masterData = codeEntityService.selectFromMaster();
        System.out.println("主数据源查询结果数量: " + masterData.size());
        
        // 从从数据源查询
        List<CodeEntityPO> slaveData = codeEntityService.selectFromSlave();
        System.out.println("从数据源查询结果数量: " + slaveData.size());
    }

    /**
     * 测试编程式动态切换
     */
    @Test
    public void testProgrammaticSwitch() {
        System.out.println("=== 测试编程式动态切换 ===");
        
        // 使用字符串指定数据源
        List<CodeEntityPO> masterData = codeEntityService.selectByDataSource("master");
        System.out.println("动态切换到主数据源查询结果数量: " + masterData.size());
        
        List<CodeEntityPO> slaveData = codeEntityService.selectByDataSource("slave");
        System.out.println("动态切换到从数据源查询结果数量: " + slaveData.size());
        
        // 使用枚举指定数据源
        List<CodeEntityPO> masterDataEnum = codeEntityService.selectByDataSource(DataSourceEnum.MASTER);
        System.out.println("使用枚举切换到主数据源查询结果数量: " + masterDataEnum.size());
    }

    /**
     * 测试自定义注解方式
     */
    @Test
    public void testCustomAnnotation() {
        System.out.println("=== 测试自定义注解方式 ===");
        
        // 使用注解切换数据源
        List<CodeEntityPO> masterData = advancedService.selectFromMasterWithAnnotation();
        System.out.println("注解方式主数据源查询结果数量: " + masterData.size());
        
        List<CodeEntityPO> slaveData = advancedService.selectFromSlaveWithAnnotation();
        System.out.println("注解方式从数据源查询结果数量: " + slaveData.size());
        
        // 使用枚举注解切换数据源
        List<CodeEntityPO> masterDataEnum = advancedService.selectFromMasterWithEnum();
        System.out.println("枚举注解方式主数据源查询结果数量: " + masterDataEnum.size());
        
        List<CodeEntityPO> slaveDataEnum = advancedService.selectFromSlaveWithEnum();
        System.out.println("枚举注解方式从数据源查询结果数量: " + slaveDataEnum.size());
    }

    /**
     * 测试数据保存到不同数据源
     */
    @Test
    public void testSaveToDataSource() {
        System.out.println("=== 测试数据保存到不同数据源 ===");
        
        // 创建测试数据
        CodeEntityPO entity = new CodeEntityPO();
        entity.setUsername("testUser");
        entity.setCode("TEST001");
        entity.setValid("Y");
        entity.setVersion(new BigDecimal("1.0"));
        entity.setCodeValidTime(LocalDateTime.now().plusDays(30));
        entity.setUpdateTime(LocalDateTime.now());
        
        // 保存到主数据源
        boolean masterSaveResult = codeEntityService.saveByDataSource(entity, "master");
        System.out.println("保存到主数据源结果: " + masterSaveResult);
        
        // 修改数据
        entity.setCode("TEST002");
        
        // 保存到从数据源
        boolean slaveSaveResult = codeEntityService.saveByDataSource(entity, "slave");
        System.out.println("保存到从数据源结果: " + slaveSaveResult);
    }

    /**
     * 测试批量操作
     */
    @Test
    public void testBatchOperation() {
        System.out.println("=== 测试批量操作 ===");

        CodeEntityPO entity = new CodeEntityPO();
        entity.setUsername("batchUser");
        entity.setCode("BATCH001");
        entity.setValid("Y");
        entity.setVersion(new BigDecimal("1.0"));
        entity.setCodeValidTime(LocalDateTime.now().plusDays(30));
        entity.setUpdateTime(LocalDateTime.now());

        String result = advancedService.batchOperation(entity);
        System.out.println("批量操作结果: " + result);
    }

    /**
     * 测试手动数据源切换
     */
    @Test
    public void testManualDataSourceSwitch() {
        System.out.println("=== 测试手动数据源切换 ===");

        // 测试手动保存用户
        CodeEntityPO entity = new CodeEntityPO();
        entity.setUsername("manualUser");
        entity.setCode("MANUAL001");
        entity.setValid("Y");
        entity.setVersion(new BigDecimal("1.0"));
        entity.setCodeValidTime(LocalDateTime.now().plusDays(30));
        entity.setUpdateTime(LocalDateTime.now());

        boolean saveResult = manualService.saveUser(entity);
        System.out.println("手动保存用户结果: " + saveResult);

        // 测试从主库查询
        List<CodeEntityPO> masterUsers = manualService.getUsersFromMaster();
        System.out.println("主库用户数量: " + masterUsers.size());

        // 测试从从库查询
        List<CodeEntityPO> slaveUsers = manualService.getUsersFromSlave();
        System.out.println("从库用户数量: " + slaveUsers.size());
    }

    /**
     * 测试复杂业务操作
     */
    @Test
    public void testComplexManualOperation() {
        System.out.println("=== 测试复杂手动操作 ===");

        CodeEntityPO entity = new CodeEntityPO();
        entity.setUsername("complexUser");
        entity.setCode("COMPLEX001");
        entity.setValid("Y");
        entity.setVersion(new BigDecimal("1.0"));
        entity.setCodeValidTime(LocalDateTime.now().plusDays(30));
        entity.setUpdateTime(LocalDateTime.now());

        String result = manualService.complexBusinessOperation(entity);
        System.out.println("复杂操作结果: " + result);
    }

    /**
     * 测试AOP自动管理
     */
    @Test
    public void testAutoCleanupDataSource() {
        System.out.println("=== 测试AOP自动管理 ===");

        // 测试主库
        List<CodeEntityPO> masterData = manualService.getUsersWithAutoCleanup("master");
        System.out.println("AOP管理-主库数据量: " + masterData.size());

        // 测试从库
        List<CodeEntityPO> slaveData = manualService.getUsersWithAutoCleanup("slave");
        System.out.println("AOP管理-从库数据量: " + slaveData.size());
    }

    /**
     * 测试批量统计操作
     */
    @Test
    public void testBatchStats() {
        System.out.println("=== 测试批量统计操作 ===");

        String result = manualService.batchOperationWithManualSwitch();
        System.out.println("批量统计结果: " + result);
    }
}
