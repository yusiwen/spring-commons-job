package cn.yusiwen.spring.commons.job.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a scheduled task definition persisted in the database.
 * <p>Stores the cron schedule, target bean reference, execution parameters,
 * and the status of the last execution.</p>
 */
@Data
public class TaskInfo {

    /** Unique identifier (UUID without hyphens). */
    private String id;

    /** The display name of the task. */
    private String name;

    /** Cron expression defining the execution schedule. */
    private String cronExpression;

    /** Name of the Spring bean to invoke. */
    private String beanName;

    /** Method name to invoke on the bean. */
    private String methodName;

    /** JSON parameters for the target method. */
    private String params;

    /** Task status: {@code ENABLED} or {@code DISABLED}. */
    private String status;

    /** Execution mode: {@code LOCAL} or {@code CALLBACK} (reserved for distributed future). */
    private String executeMode;

    /** Callback URL for {@code CALLBACK} execution mode. */
    private String callbackUrl;

    /** Whether concurrent execution is allowed. */
    private Boolean concurrent;

    /** The last time the task was triggered. */
    private LocalDateTime lastTriggerAt;

    /** The last time execution ended. */
    private LocalDateTime lastEndAt;

    /** Result of the last execution: {@code SUCCESS} or {@code FAIL}. */
    private String lastResult;

    /** Record creation timestamp. */
    private LocalDateTime createdAt;

    /** Record last update timestamp. */
    private LocalDateTime updatedAt;
}
