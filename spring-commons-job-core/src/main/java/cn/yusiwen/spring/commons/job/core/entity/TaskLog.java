package cn.yusiwen.spring.commons.job.core.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskLog {
    private String id;
    private String taskId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private String triggerType;
    private String taskParams;
    private String executionHost;
    private String result;
    private String errorMessage;
    private LocalDateTime createdAt;
}
