package cn.yusiwen.spring.commons.job.core.annotation;

import cn.yusiwen.spring.commons.job.core.config.JobManagementConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables the job management module.
 * <p>Add this annotation to a {@code @Configuration} class to manually enable
 * the job management infrastructure, including task scheduling, REST endpoints,
 * and database schema initialization. When using {@code spring-commons-job-starter},
 * this annotation is optional as auto-configuration is enabled by default.</p>
 *
 * @see JobManagementConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JobManagementConfiguration.class)
public @interface EnableJobManagement {
}
