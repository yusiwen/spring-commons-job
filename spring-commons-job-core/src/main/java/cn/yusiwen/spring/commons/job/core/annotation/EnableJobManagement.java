package cn.yusiwen.spring.commons.job.core.annotation;

import cn.yusiwen.spring.commons.job.core.config.JobManagementConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JobManagementConfiguration.class)
public @interface EnableJobManagement {
}
