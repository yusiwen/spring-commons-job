package cn.yusiwen.spring.commons.job.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ScheduledTask {

    String name();

    String cron();

    String method() default "execute";

    String params() default "";

    boolean concurrent() default false;
}
