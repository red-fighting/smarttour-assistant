package com.panduoma.trevaljava.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis 配置类
 * 手动创建 SqlSessionFactory 并加载 XML 映射文件。
 * 注意：手动创建会覆盖 Spring Boot 自动配置，因此必须显式设置 mapperLocations，
 * 否则 application.yml 中的 mybatis.mapper-locations 不生效，XML 中定义的 SQL 语句全部找不到。
 */
@Configuration
public class MyBatisConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        // 加载 XML 映射文件（必须设置，否则 XML 中的 SQL 语句不会被注册）
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mapper/*.xml")
        );
        // 开启驼峰命名转换（与 application.yml 中 map-underscore-to-camel-case 等效）
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(configuration);

        return factoryBean.getObject();
    }
}