package cn.yusiwen.spring.commons.job.core.manager;

import cn.yusiwen.spring.commons.job.core.entity.TaskInfo;
import cn.yusiwen.spring.commons.job.core.entity.TaskLog;
import cn.yusiwen.spring.commons.job.core.executor.TaskExecutor;
import cn.yusiwen.spring.commons.job.core.mapper.TaskInfoMapper;
import cn.yusiwen.spring.commons.job.core.mapper.TaskLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSchedulerManager {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final TaskInfoMapper taskInfoMapper;
    private final TaskLogMapper taskLogMapper;
    private final TaskExecutor taskExecutor;

    private final Map<String, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> runningFlags = new ConcurrentHashMap<>();

    public void scheduleTask(TaskInfo task) {
        cancelTask(task.getId());
        if (!"ENABLED".equals(task.getStatus())) {
            return;
        }
        if (task.getCronExpression() == null || task.getCronExpression().isEmpty()) {
            log.warn("Task [{}] has no cron expression, skipping schedule", task.getName());
            return;
        }
        ScheduledFuture<?> future = taskScheduler.schedule(
                createRunnable(task),
                new CronTrigger(task.getCronExpression())
        );
        scheduledFutures.put(task.getId(), future);
        log.info("Scheduled task [{}] with cron [{}]", task.getName(), task.getCronExpression());
    }

    public void cancelTask(String taskId) {
        ScheduledFuture<?> future = scheduledFutures.remove(taskId);
        if (future != null) {
            future.cancel(false);
            log.debug("Cancelled task [{}]", taskId);
        }
    }

    public boolean isScheduled(String taskId) {
        return scheduledFutures.containsKey(taskId);
    }

    public boolean isRunning(String taskId) {
        AtomicBoolean flag = runningFlags.get(taskId);
        return flag != null && flag.get();
    }

    public void cancelAll() {
        for (String taskId : scheduledFutures.keySet()) {
            cancelTask(taskId);
        }
    }

    @PreDestroy
    public void destroy() {
        cancelAll();
    }

    private Runnable createRunnable(TaskInfo task) {
        return () -> {
            String taskId = task.getId();
            AtomicBoolean flag = runningFlags.computeIfAbsent(taskId, k -> new AtomicBoolean(false));

            if (!task.getConcurrent() && !flag.compareAndSet(false, true)) {
                log.warn("Task [{}] is already running, skipped", task.getName());
                return;
            }

            TaskLog taskLog = new TaskLog();
            taskLog.setId(UUID.randomUUID().toString().replace("-", ""));
            taskLog.setTaskId(taskId);
            taskLog.setStartTime(LocalDateTime.now());
            taskLog.setCreatedAt(LocalDateTime.now());

            try {
                task.setLastTriggerAt(LocalDateTime.now());
                taskExecutor.execute(task);
                task.setLastResult("SUCCESS");
                task.setLastEndAt(LocalDateTime.now());
                taskLog.setResult("SUCCESS");
                log.info("Task [{}] executed successfully", task.getName());
            } catch (Exception e) {
                task.setLastResult("FAIL");
                task.setLastEndAt(LocalDateTime.now());
                taskLog.setResult("FAIL");
                taskLog.setErrorMessage(e.getMessage());
                log.error("Task [{}] execution failed: {}", task.getName(), e.getMessage(), e);
            } finally {
                flag.set(false);
                try {
                    taskInfoMapper.update(task);
                    taskLog.setEndTime(LocalDateTime.now());
                    taskLogMapper.insert(taskLog);
                } catch (Exception e) {
                    log.error("Failed to persist task execution result for [{}]", task.getName(), e);
                }
            }
        };
    }
}
