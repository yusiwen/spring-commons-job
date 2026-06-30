package cn.yusiwen.spring.commons.job.core.runner;

import cn.yusiwen.spring.commons.job.core.entity.TaskInfo;
import cn.yusiwen.spring.commons.job.core.manager.TaskSchedulerManager;
import cn.yusiwen.spring.commons.job.core.mapper.TaskInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@link CommandLineRunner} that loads all enabled tasks from the database
 * and registers them with the scheduler on application startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskInitRunner implements CommandLineRunner {

    private final TaskInfoMapper taskInfoMapper;
    private final TaskSchedulerManager schedulerManager;

    /**
     * Loads all enabled tasks from the database and schedules them.
     *
     * @param args application command-line arguments (unused)
     */
    @Override
    public void run(String... args) {
        List<TaskInfo> enabledTasks = taskInfoMapper.findByStatus("ENABLED");
        for (TaskInfo task : enabledTasks) {
            schedulerManager.scheduleTask(task);
        }
        log.info("Initialized {} scheduled tasks from database", enabledTasks.size());
    }
}
