package cn.yusiwen.spring.commons.job.test;

import cn.yusiwen.spring.commons.job.core.annotation.ScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@ScheduledTask(name = "annotated-task", cron = "0 */5 * * * ?")
@Component("scheduledTaskTestBean")
public class ScheduledTaskTestBean {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskTestBean.class);

    private final AtomicLong counter = new AtomicLong(0);

    public void execute() {
        long count = counter.incrementAndGet();
        log.info("ScheduledTaskTestBean.execute() called, count = {}", count);
    }

    public long getInvocationCount() {
        return counter.get();
    }
}
