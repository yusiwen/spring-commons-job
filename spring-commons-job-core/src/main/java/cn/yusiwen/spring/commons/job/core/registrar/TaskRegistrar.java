package cn.yusiwen.spring.commons.job.core.registrar;

import cn.yusiwen.spring.commons.job.core.dto.TaskInfoRequest;
import cn.yusiwen.spring.commons.job.core.entity.TaskInfo;
import cn.yusiwen.spring.commons.job.core.entity.TaskLog;
import cn.yusiwen.spring.commons.job.core.mapper.TaskInfoMapper;
import cn.yusiwen.spring.commons.job.core.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class TaskRegistrar {

    private static final Logger log = LoggerFactory.getLogger(TaskRegistrar.class);

    private final TaskService taskService;
    private final ApplicationContext applicationContext;
    private final TaskInfoMapper taskInfoMapper;

    public TaskRegistrar(TaskService taskService,
                         ApplicationContext applicationContext,
                         TaskInfoMapper taskInfoMapper) {
        this.taskService = taskService;
        this.applicationContext = applicationContext;
        this.taskInfoMapper = taskInfoMapper;
    }

    public TaskDefinition schedule(String name, String cron, Object bean, String methodName) {
        String[] beanNames = applicationContext.getBeanNamesForType(bean.getClass());
        if (beanNames.length == 0) {
            throw new IllegalArgumentException("No bean found for type: " + bean.getClass().getName());
        }
        return new TaskDefinition(this, name, cron, beanNames[0], methodName);
    }

    public TaskDefinition schedule(String name, String cron, String beanName, String methodName) {
        return new TaskDefinition(this, name, cron, beanName, methodName);
    }

    public String schedule(String name, String cron, Runnable task) {
        String beanName = "__job_" + name;
        registerRunnableBean(beanName, task);
        TaskDefinition def = new TaskDefinition(this, name, cron, beanName, "execute");
        return def.register();
    }

    public void cancel(String taskId) {
        taskService.disableTask(taskId);
    }

    public void cancelByName(String name) {
        TaskInfo task = taskInfoMapper.findByName(name);
        if (task != null) {
            taskService.disableTask(task.getId());
        }
    }

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
