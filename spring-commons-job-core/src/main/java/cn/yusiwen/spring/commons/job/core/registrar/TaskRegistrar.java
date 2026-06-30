package cn.yusiwen.spring.commons.job.core.registrar;

import cn.yusiwen.spring.commons.job.core.dto.TaskInfoRequest;
import cn.yusiwen.spring.commons.job.core.entity.TaskInfo;
import cn.yusiwen.spring.commons.job.core.mapper.TaskInfoMapper;
import cn.yusiwen.spring.commons.job.core.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Programmatic entry point for registering scheduled tasks at runtime.
 * <p>Supports three registration modes:
 * <ul>
 *   <li>Reference an existing Spring bean method via object instance</li>
 *   <li>Reference a bean by its name</li>
 *   <li>Wrap a {@link Runnable} as a dynamically registered bean</li>
 * </ul>
 */
@Component
public class TaskRegistrar {

    private static final Logger log = LoggerFactory.getLogger(TaskRegistrar.class);

    private final TaskService taskService;
    private final ApplicationContext applicationContext;
    private final TaskInfoMapper taskInfoMapper;

    /**
     * Creates a new {@link TaskRegistrar}.
     *
     * @param taskService        the task service for persistence
     * @param applicationContext the Spring application context
     * @param taskInfoMapper     the mapper for looking up tasks by name
     */
    public TaskRegistrar(TaskService taskService,
                         ApplicationContext applicationContext,
                         TaskInfoMapper taskInfoMapper) {
        this.taskService = taskService;
        this.applicationContext = applicationContext;
        this.taskInfoMapper = taskInfoMapper;
    }

    /**
     * Registers a task that references a method on an existing Spring bean instance.
     * <p>The bean name is resolved automatically from the application context.</p>
     *
     * @param name       the display name of the task
     * @param cron       the cron expression
     * @param bean       the bean instance (must be a Spring-managed bean)
     * @param methodName the method name to invoke
     * @return a {@link TaskDefinition} for additional configuration
     * @throws IllegalArgumentException if no bean is found for the given type
     */
    public TaskDefinition schedule(String name, String cron, Object bean, String methodName) {
        String[] beanNames = applicationContext.getBeanNamesForType(bean.getClass());
        if (beanNames.length == 0) {
            throw new IllegalArgumentException("No bean found for type: " + bean.getClass().getName());
        }
        return new TaskDefinition(this, name, cron, beanNames[0], methodName);
    }

    /**
     * Registers a task that references a bean by its name.
     *
     * @param name       the display name of the task
     * @param cron       the cron expression
     * @param beanName   the name of the Spring bean
     * @param methodName the method name to invoke
     * @return a {@link TaskDefinition} for additional configuration
     */
    public TaskDefinition schedule(String name, String cron, String beanName, String methodName) {
        return new TaskDefinition(this, name, cron, beanName, methodName);
    }

    /**
     * Registers a {@link Runnable} as a scheduled task.
     * <p>The runnable is automatically wrapped as a Spring bean with an
     * {@code execute()} method and registered dynamically in the application
     * context.</p>
     *
     * @param name the display name of the task
     * @param cron the cron expression
     * @param task the runnable to execute
     * @return the ID of the created task
     * @throws IllegalStateException if the application context is not configurable
     */
    public String schedule(String name, String cron, Runnable task) {
        String beanName = "__job_" + name;
        registerRunnableBean(beanName, task);
        TaskDefinition def = new TaskDefinition(this, name, cron, beanName, "execute");
        return def.register();
    }

    /**
     * Disables a task by its ID.
     *
     * @param taskId the task ID to cancel
     */
    public void cancel(String taskId) {
        taskService.disableTask(taskId);
    }

    /**
     * Disables a task by its name.
     *
     * @param name the display name of the task
     */
    public void cancelByName(String name) {
        TaskInfo task = taskInfoMapper.findByName(name);
        if (task != null) {
            taskService.disableTask(task.getId());
        }
    }

    /**
     * Persists a task definition and activates its scheduling.
     *
     * @param name       the display name
     * @param cron       the cron expression
     * @param beanName   the Spring bean name
     * @param methodName the method name
     * @param params     optional JSON parameters
     * @param concurrent whether concurrent execution is allowed
     * @return the ID of the created task
     */
    String doRegister(String name, String cron, String beanName, String methodName,
                       String params, boolean concurrent) {
        TaskInfoRequest request = new TaskInfoRequest();
        request.setName(name);
        request.setCronExpression(cron);
        request.setBeanName(beanName);
        request.setMethodName(methodName);
        request.setParams(params);
        request.setConcurrent(concurrent);
        return taskService.createTask(request).getId();
    }

    /**
     * Dynamically registers a {@link RunnableWrapper} bean in the application context.
     *
     * @param beanName the bean name to use
     * @param runnable the runnable to wrap
     * @throws IllegalStateException if the context is not configurable
     */
    private void registerRunnableBean(String beanName, Runnable runnable) {
        if (applicationContext.containsBean(beanName)) {
            log.debug("Bean [{}] already exists, skipping registration", beanName);
            return;
        }
        if (!(applicationContext instanceof ConfigurableApplicationContext)) {
            throw new IllegalStateException("ApplicationContext is not configurable");
        }
        ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) applicationContext;
        DefaultListableBeanFactory bf = (DefaultListableBeanFactory) ctx.getBeanFactory();

        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(RunnableWrapper.class)
                .addConstructorArgValue(runnable);
        bf.registerBeanDefinition(beanName, builder.getBeanDefinition());
        log.debug("Registered dynamic bean [{}] for Runnable task", beanName);
    }
}
