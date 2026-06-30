package cn.yusiwen.spring.commons.job.core.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskInfoVO {
    private String id;
    private String name;
    private String cronExpression;
    private String beanName;
    private String methodName;
    private String params;
    private String status;
    private String executeMode;
    private String callbackUrl;
    private Boolean concurrent;
    private Boolean running;
    private LocalDateTime lastTriggerAt;
    private LocalDateTime lastEndAt;
    private String lastResult;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
