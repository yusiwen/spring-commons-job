package cn.yusiwen.spring.commons.job.starter;

import cn.yusiwen.spring.commons.job.core.config.JobManagementConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(JobManagementConfiguration.class)
public class JobAutoConfiguration {
}
