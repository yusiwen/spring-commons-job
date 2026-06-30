package cn.yusiwen.spring.commons.job.core.registrar;

/**
 * Internal adapter that wraps a {@link Runnable} as a Spring bean with an
 * {@code execute()} method, enabling dynamic task registration.
 * <p>Used by {@link TaskRegistrar#schedule(String, String, Runnable)} to
 * bridge {@link Runnable} instances with the reflection-based
 * {@link cn.yusiwen.spring.commons.job.core.executor.LocalTaskExecutor}.</p>
 */
public class RunnableWrapper {

    private final Runnable runnable;

    /**
     * Creates a new wrapper.
     *
     * @param runnable the runnable to wrap
     */
    public RunnableWrapper(Runnable runnable) {
        this.runnable = runnable;
    }

    /**
     * Delegates to the wrapped {@link Runnable#run()} method.
     */
    public void execute() {
        runnable.run();
    }
}
