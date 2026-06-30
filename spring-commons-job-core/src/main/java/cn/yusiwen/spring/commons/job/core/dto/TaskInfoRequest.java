package cn.yusiwen.spring.commons.job.core.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Request DTO for creating or updating a scheduled task.
 */
@Data
public class TaskInfoRequest {

    /** The display name of the task. */
    @NotBlank(message = "Task name is required")
    private String name;

    /** Cron expression for scheduling. */
    @NotBlank(message = "Cron expression is required")
    private String cronExpression;

    /** Name of the Spring bean to invoke. */
    @NotBlank(message = "Bean name is required")
    private String beanName;

    /** Method name to invoke on the bean. */
    @NotBlank(message = "Method name is required")
    private String methodName;

    /** JSON parameters for the target method. */
    private String params;

    /** Whether concurrent execution is allowed. Defaults to {@code false}. */
    private Boolean concurrent = false;
}
