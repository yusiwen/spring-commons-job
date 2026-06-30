package cn.yusiwen.spring.commons.job.core.registrar;

public class TaskDefinition {

    private final TaskRegistrar registrar;
    private final String name;
    private final String cron;
    private final String beanName;
    private final String methodName;
    private String params;
    private boolean concurrent;

    TaskDefinition(TaskRegistrar registrar, String name, String cron,
                   String beanName, String methodName) {
        this.registrar = registrar;
        this.name = name;
        this.cron = cron;
        this.beanName = beanName;
        this.methodName = methodName;
    }

    public TaskDefinition params(String params) {
        this.params = params;
        return this;
    }

    public TaskDefinition concurrent(boolean concurrent) {
        this.concurrent = concurrent;
        return this;
    }

    public String register() {
        return registrar.doRegister(name, cron, beanName, methodName, params, concurrent);
    }
}
