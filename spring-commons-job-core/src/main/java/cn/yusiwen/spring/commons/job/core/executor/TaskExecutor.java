package cn.yusiwen.spring.commons.job.core.executor;

import cn.yusiwen.spring.commons.job.core.entity.TaskInfo;

/**
 * Strategy interface for executing scheduled tasks.
 * <p>Implementations determine how a task is invoked — locally via reflection
 * ({@link LocalTaskExecutor}), or remotely via HTTP callback for distributed
 * scenarios (future implementation).</p>
 */
public interface TaskExecutor {

    /**
     * Executes the given scheduled task.
     *
     * @param taskInfo the task definition containing bean/method/params information
     * @throws Exception if execution fails
     */
    void execute(TaskInfo taskInfo) throws Exception;

    /**
     * Returns the type identifier of this executor.
     *
     * @return the executor type (e.g., {@code "LOCAL"}, {@code "CALLBACK"})
     */
    String getType();
}
