package cn.yusiwen.spring.commons.job.core.annotation;

import java.lang.annotation.*;

/**
 * Marks a Spring bean as a scheduled task that is automatically registered
 * when the application context is refreshed.
 * <p>The annotated class must be a Spring-managed component (e.g., annotated
 * with {@code @Component}). The method specified by {@link #method()} is
 * invoked on the cron schedule defined by {@link #cron()}.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ScheduledTask {

    /**
     * The display name of the task. Must be unique among all registered tasks.
     * @return the task name
     */
    String name();

    /**
     * A cron expression defining the execution schedule.
     * @return the cron expression
     */
    String cron();

    /**
     * The method name to invoke on the bean.
     * @return the method name
     */
    String method() default "execute";

    /**
     * Optional JSON parameters passed to the target method.
     * @return the JSON parameter string
     */
    String params() default "";

    /**
     * Whether concurrent execution is allowed.
     * @return {@code true} if multiple executions may overlap
     */
    boolean concurrent() default false;
}
