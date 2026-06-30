package cn.yusiwen.spring.commons.job.core.config;

import cn.yusiwen.spring.commons.job.core.executor.LocalTaskExecutor;
import cn.yusiwen.spring.commons.job.core.executor.TaskExecutor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.sql.DataSource;

@Configuration
@ComponentScan("cn.yusiwen.spring.commons.job.core")
@MapperScan("cn.yusiwen.spring.commons.job.core.mapper")
@EnableConfigurationProperties(JobManagementProperties.class)
@ConditionalOnProperty(prefix = "job.management", name = "enabled", matchIfMissing = true)
public class JobManagementConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(JobManagementProperties properties) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(properties.getPoolSize());
        scheduler.setThreadNamePrefix("job-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }

    @Bean
    @ConditionalOnMissingBean(TaskExecutor.class)
    public TaskExecutor taskExecutor(ApplicationContext applicationContext) {
        return new LocalTaskExecutor(applicationContext);
    }

    @Configuration
    @ConditionalOnClass(name = "org.flywaydb.core.Flyway")
    static class FlywaySchemaConfig {

        @Bean
        JobSchemaFlywayInitializer jobSchemaFlywayInitializer(DataSource dataSource) {
            return new JobSchemaFlywayInitializer(dataSource);
        }
    }

    @Configuration
    @ConditionalOnMissingClass("org.flywaydb.core.Flyway")
    static class FallbackSchemaConfig {

        @Bean
        DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new org.springframework.core.io.ClassPathResource("sql/job-schema.sql"));
            populator.setContinueOnError(true);
            DataSourceInitializer initializer = new DataSourceInitializer();
            initializer.setDataSource(dataSource);
            initializer.setDatabasePopulator(populator);
            return initializer;
        }
    }
}
