package cn.yusiwen.spring.commons.job.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component("testTaskBean")
public class TestTaskBean {

    private final AtomicLong counter = new AtomicLong(0);

    public void execute() {
        long count = counter.incrementAndGet();
        log.info("TestTaskBean.execute() called, count = {}", count);
    }

    public void executeWithParam(String param) {
        log.info("TestTaskBean.executeWithParam() called, param = {}", param);
    }

    public long getInvocationCount() {
        return counter.get();
    }
}
