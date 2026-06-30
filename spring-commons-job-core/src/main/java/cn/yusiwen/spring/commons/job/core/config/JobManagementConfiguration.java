package cn.yusiwen.spring.commons.job.core.config;

import cn.yusiwen.spring.commons.job.core.executor.LocalTaskExecutor;
import cn.yusiwen.spring.commons.job.core.executor.TaskExecutor;
import org.mybatis.spring.annotation.MapperScan;
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

/**
 * Core configuration class for the job management module.
 * <p>Scans for components in the base package, registers MyBatis mappers,
 * and configures the task scheduler, executor, and dual-channel schema
 * initialization (Flyway or DataSourceInitializer fallback).</p>
 *
 * <p>This configuration is loaded either by {@link
 * cn.yusiwen.spring.commons.job.core.annotation.EnableJobManagement} or by the
 * auto-configuration in {@code spring-commons-job-starter}.</p>
 */
@Configuration
@ComponentScan("cn.yusiwen.spring.commons.job.core")
@MapperScan("cn.yusiwen.spring.commons.job.core.mapper")
@EnableConfigurationProperties(JobManagementProperties.class)
@ConditionalOnProperty(prefix = "job.management", name = "enabled", matchIfMissing = true)
public class JobManagementConfiguration {

    /**
     * Creates the {@link ThreadPoolTaskScheduler} used for scheduling tasks.
     *
     * @param properties the job management configuration properties
     * @return a configured {@link ThreadPoolTaskScheduler}
     */
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

    /**
     * Creates the default {@link TaskExecutor} that invokes methods locally via reflection.
     *
     * @param applicationContext the Spring application context
     * @return a {@link LocalTaskExecutor} instance
     */
    @Bean
    @ConditionalOnMissingBean(TaskExecutor.class)
    public TaskExecutor taskExecutor(ApplicationContext applicationContext) {
        return new LocalTaskExecutor(applicationContext);
    }

    /**
     * Schema initialization via Flyway when the dependency is present on the classpath.
     * Uses an isolated schema history table to avoid conflicts with the user's Flyway setup.
     */
    @Configuration
    @ConditionalOnClass(name = "org.flywaydb.core.Flyway")
    static class FlywaySchemaConfig {

        /**
         * Creates a {@link JobSchemaFlywayInitializer} that runs Flyway migrations.
         *
         * @param dataSource the application data source
         * @return a new {@link JobSchemaFlywayInitializer}
         */
        @Bean
        JobSchemaFlywayInitializer jobSchemaFlywayInitializer(DataSource dataSource) {
            return new JobSchemaFlywayInitializer(dataSource);
        }
    }

    /**
     * Fallback schema initialization via {@link DataSourceInitializer} when Flyway
     * is not on the classpath.
     */
    @Configuration
    @ConditionalOnMissingClass("org.flywaydb.core.Flyway")
    static class FallbackSchemaConfig {

        /**
         * Creates a {@link DataSourceInitializer} that runs the fallback SQL script.
         *
         * @param dataSource the application data source
         * @return a configured {@link DataSourceInitializer}
         */
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
