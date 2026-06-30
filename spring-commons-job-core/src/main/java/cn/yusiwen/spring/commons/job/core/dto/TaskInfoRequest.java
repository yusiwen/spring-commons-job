package cn.yusiwen.spring.commons.job.core.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TaskInfoRequest {

    @NotBlank(message = "Task name is required")
    private String name;

    @NotBlank(message = "Cron expression is required")
    private String cronExpression;

    @NotBlank(message = "Bean name is required")
    private String beanName;

    @NotBlank(message = "Method name is required")
    private String methodName;

    private String params;

    private Boolean concurrent = false;
}
