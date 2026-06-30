package cn.yusiwen.spring.commons.job.core.registrar;

public class RunnableWrapper {

    private final Runnable runnable;

    public RunnableWrapper(Runnable runnable) {
        this.runnable = runnable;
    }

    public void execute() {
        runnable.run();
    }
}
