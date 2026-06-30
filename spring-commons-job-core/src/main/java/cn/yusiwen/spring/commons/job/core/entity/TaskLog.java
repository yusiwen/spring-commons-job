package cn.yusiwen.spring.commons.job.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a single execution record of a scheduled task.
 * <p>Captures the start and end times, execution duration, result,
 * triggered-by information, and error details.</p>
 */
@Data
public class TaskLog {

    /** Unique identifier (UUID without hyphens). */
    private String id;

    /** The task ID this log entry belongs to. */
    private String taskId;

    /** The time execution started. */
    private LocalDateTime startTime;

    /** The time execution ended. */
    private LocalDateTime endTime;

    /** Execution duration in milliseconds. */
    private Long durationMs;

    /** Trigger type: {@code CRON} for scheduled or {@code MANUAL} for manual trigger. */
    private String triggerType;

    /** Snapshot of the task parameters at the time of execution. */
    private String taskParams;

    /** Hostname or identifier of the executing node (reserved for distributed future). */
    private String executionHost;

    /** Execution result: {@code SUCCESS} or {@code FAIL}. */
    private String result;

    /** Error message if execution failed. */
    private String errorMessage;

    /** Record creation timestamp. */
    private LocalDateTime createdAt;
}
