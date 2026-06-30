package cn.yusiwen.spring.commons.job.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "job.management")
public class JobManagementProperties {

    private boolean enabled = true;

    private int poolSize = 5;
}
