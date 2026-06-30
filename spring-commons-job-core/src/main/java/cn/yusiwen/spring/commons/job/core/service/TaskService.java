package cn.yusiwen.spring.commons.job.core.service;

import cn.yusiwen.spring.commons.job.core.dto.PageResult;
import cn.yusiwen.spring.commons.job.core.dto.TaskInfoRequest;
import cn.yusiwen.spring.commons.job.core.dto.TaskInfoVO;
import cn.yusiwen.spring.commons.job.core.entity.TaskInfo;
import cn.yusiwen.spring.commons.job.core.entity.TaskLog;
import cn.yusiwen.spring.commons.job.core.executor.TaskExecutor;
import cn.yusiwen.spring.commons.job.core.mapper.TaskInfoMapper;
import cn.yusiwen.spring.commons.job.core.mapper.TaskLogMapper;
import cn.yusiwen.spring.commons.job.core.manager.TaskSchedulerManager;
import cn.yusiwen.spring.commons.job.core.converter.TaskConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskInfoMapper taskInfoMapper;
    private final TaskLogMapper taskLogMapper;
    private final TaskSchedulerManager schedulerManager;
    private final TaskExecutor taskExecutor;

    @Transactional
    public TaskInfoVO createTask(TaskInfoRequest request) {
        TaskInfo task = TaskConverter.toEntity(request);
        task.setId(UUID.randomUUID().toString().replace("-", ""));
        task.setStatus("ENABLED");
        task.setExecuteMode("LOCAL");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        taskInfoMapper.insert(task);
        schedulerManager.scheduleTask(task);
        log.info("Created task [{}] with id [{}]", task.getName(), task.getId());
        return TaskConverter.toVO(task);
    }

    @Transactional
    public TaskInfoVO updateTask(String id, TaskInfoRequest request) {
        TaskInfo task = taskInfoMapper.findById(id);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + id);
        }
        task.setName(request.getName());
        task.setCronExpression(request.getCronExpression());
        task.setBeanName(request.getBeanName());
        task.setMethodName(request.getMethodName());
        task.setParams(request.getParams());
        task.setConcurrent(request.getConcurrent());
        task.setUpdatedAt(LocalDateTime.now());

        taskInfoMapper.update(task);
        schedulerManager.scheduleTask(task);
        log.info("Updated task [{}]", id);
        return TaskConverter.toVO(task);
    }

    @Transactional
    public void deleteTask(String id) {
        schedulerManager.cancelTask(id);
        taskInfoMapper.deleteById(id);
        log.info("Deleted task [{}]", id);
    }

    public TaskInfoVO getTask(String id) {
        TaskInfo task = taskInfoMapper.findById(id);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + id);
        }
        return TaskConverter.toVO(task);
    }

    public PageResult<TaskInfoVO> listTasks(int page, int size, String status) {
        RowBounds rowBounds = new RowBounds((page - 1) * size, size);
        List<TaskInfo> tasks;
        long total;
        if (status != null && !status.isEmpty()) {
            tasks = taskInfoMapper.findByStatusWithRowBounds(status, rowBounds);
            total = taskInfoMapper.countByStatus(status);
        } else {
            tasks = taskInfoMapper.findAll(rowBounds);
            total = taskInfoMapper.countAll();
        }
        List<TaskInfoVO> vos = tasks.stream()
                .map(TaskConverter::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(vos, page, size, total);
    }

    public void triggerTask(String id) throws Exception {
        TaskInfo task = taskInfoMapper.findById(id);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + id);
        }
        log.info("Manually triggering task [{}]", task.getName());
        task.setLastTriggerAt(LocalDateTime.now());
        try {
            taskExecutor.execute(task);
            task.setLastResult("SUCCESS");
        } catch (Exception e) {
            task.setLastResult("FAIL");
            task.setLastEndAt(LocalDateTime.now());
            throw e;
        } finally {
            task.setLastEndAt(LocalDateTime.now());
            taskInfoMapper.update(task);
        }
    }

    @Transactional
    public TaskInfoVO enableTask(String id) {
        TaskInfo task = taskInfoMapper.findById(id);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + id);
        }
        task.setStatus("ENABLED");
        task.setUpdatedAt(LocalDateTime.now());
        taskInfoMapper.update(task);
        schedulerManager.scheduleTask(task);
        log.info("Enabled task [{}]", id);
        return TaskConverter.toVO(task);
    }

    @Transactional
    public TaskInfoVO disableTask(String id) {
        TaskInfo task = taskInfoMapper.findById(id);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + id);
        }
        task.setStatus("DISABLED");
        task.setUpdatedAt(LocalDateTime.now());
        taskInfoMapper.update(task);
        schedulerManager.cancelTask(id);
        log.info("Disabled task [{}]", id);
        return TaskConverter.toVO(task);
    }

    public void refreshTasks() {
        schedulerManager.cancelAll();
        List<TaskInfo> enabledTasks = taskInfoMapper.findByStatus("ENABLED");
        for (TaskInfo task : enabledTasks) {
            schedulerManager.scheduleTask(task);
        }
        log.info("Refreshed {} tasks from database", enabledTasks.size());
    }

    public PageResult<TaskLog> listTaskLogs(String taskId, int page, int size) {
        RowBounds rowBounds = new RowBounds((page - 1) * size, size);
        List<TaskLog> logs = taskLogMapper.findByTaskIdWithRowBounds(taskId, rowBounds);
        long total = taskLogMapper.countByTaskId(taskId);
        return new PageResult<>(logs, page, size, total);
    }
}
