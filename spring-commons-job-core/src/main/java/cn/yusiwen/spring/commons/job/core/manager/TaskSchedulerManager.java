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
import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the lifecycle of scheduled tasks using Spring's {@link ThreadPoolTaskScheduler}.
 * <p>Maintains a registry of {@link ScheduledFuture} instances keyed by task ID,
 * providing methods to schedule, cancel, and refresh tasks dynamically. Each task
 * execution is tracked with concurrent-execution protection and logged to the
 * database.</p>
 */
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

    /**
     * Schedules or reschedules a task based on its cron expression.
     * <p>If the task is already scheduled, it is cancelled first. Only tasks
     * with status {@code ENABLED} are scheduled.</p>
     *
     * @param task the task definition to schedule
     */
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

    /**
     * Cancels a scheduled task, removing its future from the registry.
     *
     * @param taskId the ID of the task to cancel
     */
    public void cancelTask(String taskId) {
        ScheduledFuture<?> future = scheduledFutures.remove(taskId);
        if (future != null) {
            future.cancel(false);
            log.debug("Cancelled task [{}]", taskId);
        }
    }

    /**
     * Checks whether a task is currently scheduled.
     *
     * @param taskId the task ID
     * @return {@code true} if the task has an active scheduled future
     */
    public boolean isScheduled(String taskId) {
        return scheduledFutures.containsKey(taskId);
    }

    /**
     * Checks whether a task is currently executing.
     *
     * @param taskId the task ID
     * @return {@code true} if the task is currently running
     */
    public boolean isRunning(String taskId) {
        AtomicBoolean flag = runningFlags.get(taskId);
        return flag != null && flag.get();
    }

    /**
     * Cancels all currently scheduled tasks.
     */
    public void cancelAll() {
        for (String taskId : scheduledFutures.keySet()) {
            cancelTask(taskId);
        }
    }

    /**
     * Cleans up all scheduled tasks on bean destruction.
     */
    @PreDestroy
    public void destroy() {
        cancelAll();
    }

    /**
     * Creates a {@link Runnable} that executes the task, records the result,
     * and persists execution statistics to the database.
     *
     * @param task the task definition
     * @return a runnable that performs the full execution lifecycle
     */
    private Runnable createRunnable(TaskInfo task) {
        return () -> {
            String taskId = task.getId();
            AtomicBoolean flag = runningFlags.computeIfAbsent(taskId, k -> new AtomicBoolean(false));

            if (!task.getConcurrent() && !flag.compareAndSet(false, true)) {
                log.warn("Task [{}] is already running, skipped", task.getName());
                return;
            }

            LocalDateTime start = LocalDateTime.now();
            TaskLog taskLog = new TaskLog();
            taskLog.setId(UUID.randomUUID().toString().replace("-", ""));
            taskLog.setTaskId(taskId);
            taskLog.setStartTime(start);
            taskLog.setTriggerType("CRON");
            taskLog.setTaskParams(task.getParams());
            taskLog.setExecutionHost(resolveHost());
            taskLog.setCreatedAt(start);

            try {
                task.setLastTriggerAt(start);
                taskExecutor.execute(task);
                task.setLastResult("SUCCESS");
                taskLog.setResult("SUCCESS");
                log.info("Task [{}] executed successfully", task.getName());
            } catch (Exception e) {
                task.setLastResult("FAIL");
                taskLog.setResult("FAIL");
                taskLog.setErrorMessage(e.getMessage());
                log.error("Task [{}] execution failed: {}", task.getName(), e.getMessage(), e);
            } finally {
                flag.set(false);
                LocalDateTime end = LocalDateTime.now();
                task.setLastEndAt(end);
                taskLog.setEndTime(end);
                taskLog.setDurationMs(Duration.between(start, end).toMillis());
                try {
                    taskInfoMapper.update(task);
                    taskLogMapper.insert(taskLog);
                } catch (Exception e) {
                    log.error("Failed to persist task execution result for [{}]", task.getName(), e);
                }
            }
        };
    }

    /**
     * Resolves the local hostname for execution logging.
     *
     * @return the hostname, or {@code "unknown"} if resolution fails
     */
    private static String resolveHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
