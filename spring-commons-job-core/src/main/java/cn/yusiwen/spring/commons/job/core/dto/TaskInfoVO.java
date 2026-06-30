package cn.yusiwen.spring.commons.job.core.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for task information exposed via REST API.
 */
@Data
public class TaskInfoVO {

    /** Unique identifier. */
    private String id;

    /** The display name of the task. */
    private String name;

    /** Cron expression for scheduling. */
    private String cronExpression;

    /** Name of the Spring bean to invoke. */
    private String beanName;

    /** Method name to invoke on the bean. */
    private String methodName;

    /** JSON parameters for the target method. */
    private String params;

    /** Task status: {@code ENABLED} or {@code DISABLED}. */
    private String status;

    /** Execution mode: {@code LOCAL} or {@code CALLBACK}. */
    private String executeMode;

    /** Callback URL for distributed execution. */
    private String callbackUrl;

    /** Whether concurrent execution is allowed. */
    private Boolean concurrent;

    /** Whether the task is currently running. */
    private Boolean running;

    /** The last time the task was triggered. */
    private LocalDateTime lastTriggerAt;

    /** The last time execution ended. */
    private LocalDateTime lastEndAt;

    /** Result of the last execution. */
    private String lastResult;

    /** Record creation timestamp. */
    private LocalDateTime createdAt;

    /** Record last update timestamp. */
    private LocalDateTime updatedAt;
}
