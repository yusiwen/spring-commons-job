package cn.yusiwen.spring.commons.job.core.executor;

import cn.yusiwen.spring.commons.job.core.entity.TaskInfo;

public interface TaskExecutor {
    void execute(TaskInfo taskInfo) throws Exception;
    String getType();
}
