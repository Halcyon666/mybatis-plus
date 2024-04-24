package com.whalefall541;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * <p>Mybatis plus generator.</p>
 * <p>mybatis-plus-generator 3.5.6</p>
 * <p>mybatis-plus-boot-starter 3.5.4</p>
 * <p>freemarker 2.3.31</p>
 *
 * <ul><a href="https://blog.csdn.net/weixin_58170033/article/details/132721002">union primary key</a><ul/>
 * use version 1.7.2-RELEASE
 * <p>
 * <a href="https://github.com/jeffreyning/mybatisplus-plus/blob/main/README.md">union code README.md</a>
 */
public class NewCodeGenerator {

    static final String URL = "jdbc:oracle:thin:@192.168.3.161:1521:jc";
    static final String USERNAME = "jc";
    static final String PASSWORD = "123456";

    public static void main(String[] args) {
        generate();
    }


    private static void generate() {
        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
                .globalConfig(builder ->
                        builder.author("xx")
                                .commentDate("yyyy-MM-dd")
                                .disableOpenDir()
                                .outputDir(System.getProperty("user.dir")+"\\src\\main\\java")
                                .dateType(DateType.TIME_PACK)
                                .disableServiceInterface()
                )
                .packageConfig(builder ->
                        builder.parent("com.whalefall541.mybatisplus.samples.generator")
                                .moduleName("system")
                                .entity("po")
                                .serviceImpl("service.impl")
                                .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir")+"\\src\\main\\java")))
                .strategyConfig(builder ->
                        builder.addInclude("table_a")
                                .controllerBuilder()
                                .enableFileOverride()
                                .enableRestStyle()
                                .disable()

                                .entityBuilder()
                                .enableFileOverride()
                                .enableLombok()
                                .disableSerialVersionUID()
                                .enableTableFieldAnnotation()
                                .enableActiveRecord()
                                .enableFileOverride()
//                                .addTableFills(new Column("timecolunmn", FieldFill.INSERT))
                                .formatFileName("%sPO")

                                .serviceBuilder()
                                .disable()
                                .enableFileOverride()
                                .formatServiceImplFileName("%sServiceImpl")

                                .mapperBuilder()
                                .enableFileOverride()

                ).templateEngine(new FreemarkerTemplateEngine()).execute();
    }

}
