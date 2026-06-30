package cn.yusiwen.spring.commons.job.core.registrar;

/**
 * Fluent builder for configuring a task before registration.
 * <p>Obtain an instance via {@link TaskRegistrar#schedule} and call
 * {@link #register()} to persist and activate the task.</p>
 */
public class TaskDefinition {

    private final TaskRegistrar registrar;
    private final String name;
    private final String cron;
    private final String beanName;
    private final String methodName;
    private String params;
    private boolean concurrent;

    /**
     * Creates a new task definition builder.
     *
     * @param registrar  the parent registrar
     * @param name       the display name
     * @param cron       the cron expression
     * @param beanName   the Spring bean name
     * @param methodName the method name
     */
    TaskDefinition(TaskRegistrar registrar, String name, String cron,
                   String beanName, String methodName) {
        this.registrar = registrar;
        this.name = name;
        this.cron = cron;
        this.beanName = beanName;
        this.methodName = methodName;
    }

    /**
     * Sets the JSON parameters for the target method.
     *
     * @param params the JSON parameter string
     * @return this builder for chaining
     */
    public TaskDefinition params(String params) {
        this.params = params;
        return this;
    }

    /**
     * Sets whether concurrent execution is allowed.
     *
     * @param concurrent {@code true} if overlapping executions are permitted
     * @return this builder for chaining
     */
    public TaskDefinition concurrent(boolean concurrent) {
        this.concurrent = concurrent;
        return this;
    }

    /**
     * Persists the task definition and activates its scheduling.
     *
     * @return the ID of the created task
     */
    public String register() {
        return registrar.doRegister(name, cron, beanName, methodName, params, concurrent);
    }
}
