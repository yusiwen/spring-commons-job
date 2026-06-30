package cn.yusiwen.spring.commons.job.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the job management module.
 * <p>Prefix: {@code job.management}</p>
 */
@Data
@ConfigurationProperties(prefix = "job.management")
public class JobManagementProperties {

    /** Whether the job management module is enabled. Defaults to {@code true}. */
    private boolean enabled = true;

    /** Thread pool size for the task scheduler. Defaults to {@code 5}. */
    private int poolSize = 5;
}
