package com.panduoma.trevaljava.config;

import org.apache.ibatis.session.SqlSessionFactory;     // MyBatis 的核心接口，用于创建 SqlSession
import org.mybatis.spring.SqlSessionFactoryBean;        // Spring 与 MyBatis 整合的工厂 Bean，用于创建 SqlSessionFactory
import org.springframework.context.annotation.Bean;     // 告诉 Spring 这个方法返回的对象要注册为 Bean
import org.springframework.context.annotation.Configuration;    // 标记这个类是一个配置类（相当于 XML 配置文件）

import javax.sql.DataSource;        // 数据源接口，由 Spring Boot 自动配置（从 application.yml 读取）

/**
 * MyBatis 配置类
 * 作用：手动创建 MyBatis 的核心组件 SqlSessionFactory，并交给 Spring 管理。
 * 为什么需要这个配置类？
 * 默认情况下，Spring Boot 的 MyBatis Starter 会自动配置 SqlSessionFactory，
 * 但在某些版本（如 Spring Boot 4.x + MyBatis 3.0.3）中，自动配置可能因条件不满足而跳过。
 * 手动创建可以确保 SqlSessionFactory 一定存在，从而让 @Mapper 接口能够正常工作。
 * 这个配置类相当于告诉 Spring：
 *   “当应用启动时，请用这个 DataSource 创建一个 SqlSessionFactory，
 *    并把它放到 Bean 容器中，供所有的 Mapper 使用。”
 */
@Configuration      // 标明这是一个配置类，Spring 会扫描并处理其中的 @Bean 方法
public class MyBatisConfig {

    @Bean       // 这个注解表示：该方法返回的对象会被注册为 Spring 容器中的一个 Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        // 1. 创建一个 SqlSessionFactoryBean，这是 Spring 提供的工厂类，
        //    它负责解析 MyBatis 配置并生成 SqlSessionFactory 实例。
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();

        // 2. 设置数据源：告诉 MyBatis 要连接哪个数据库。
        //    Spring 会自动传入 application.yml 中配置好的 DataSource 对象。
        factoryBean.setDataSource(dataSource);
        // （可选）如果需要指定实体类包，可以取消注释下面这行：
        // factoryBean.setTypeAliasesPackage("com.panduoma.trevaljava.entity");
        // 这样 MyBatis 会自动扫描该包下的类，并注册为别名（用于 XML 中省略包名）。

        // （可选）如果需要指定 XML 映射文件位置，可以取消注释下面这行：
        // factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
        //         .getResources("classpath:mapper/**/*.xml"));
        // 如果 application.yml 中已经配置了 mapper-locations，也可以在这里再次指定。

        // 3. 调用 getObject() 方法，让 factoryBean 执行真正的初始化逻辑，
        //    并返回 SqlSessionFactory 对象。
        return factoryBean.getObject();
    }
}