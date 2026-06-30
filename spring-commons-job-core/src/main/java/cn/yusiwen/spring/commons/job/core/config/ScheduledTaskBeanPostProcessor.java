package cn.yusiwen.spring.commons.job.core.config;

import cn.yusiwen.spring.commons.job.core.annotation.ScheduledTask;
import cn.yusiwen.spring.commons.job.core.entity.TaskInfo;
import cn.yusiwen.spring.commons.job.core.mapper.TaskInfoMapper;
import cn.yusiwen.spring.commons.job.core.registrar.TaskRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * {@link ApplicationListener} that scans for beans annotated with
 * {@link ScheduledTask} on context refresh and registers them automatically.
 * <p>Duplicate task names are skipped to ensure idempotent registration.</p>
 */
@Component
public class ScheduledTaskBeanPostProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskBeanPostProcessor.class);

    private final TaskRegistrar taskRegistrar;
    private final TaskInfoMapper taskInfoMapper;

    /**
     * Creates a new processor.
     *
     * @param taskRegistrar the task registrar for registering annotated tasks
     * @param taskInfoMapper the mapper for checking existing tasks
     */
    public ScheduledTaskBeanPostProcessor(TaskRegistrar taskRegistrar, TaskInfoMapper taskInfoMapper) {
        this.taskRegistrar = taskRegistrar;
        this.taskInfoMapper = taskInfoMapper;
    }

    /**
     * Processes all beans annotated with {@link ScheduledTask} and registers them
     * if not already present in the database.
     *
     * @param event the context refresh event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null) {
            return;
        }

        Map<String, Object> beans = event.getApplicationContext()
                .getBeansWithAnnotation(ScheduledTask.class);

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            ScheduledTask ann = entry.getValue().getClass().getAnnotation(ScheduledTask.class);
            if (ann == null) {
                continue;
            }

            TaskInfo existing = taskInfoMapper.findByName(ann.name());
            if (existing != null) {
                log.debug("ScheduledTask [{}] already registered, skipping", ann.name());
                continue;
            }

            String beanName = entry.getKey();
            taskRegistrar.schedule(ann.name(), ann.cron(), beanName, ann.method())
                    .params(ann.params())
                    .concurrent(ann.concurrent())
                    .register();
            log.info("Registered @ScheduledTask [{}] with cron [{}]", ann.name(), ann.cron());
        }
    }
}
