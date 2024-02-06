package net.aopacloud.superbi.thread;

import java.util.concurrent.*;

/**
 * priority thread pool
 *
 * @author: hudong
 * @date: 2022/8/17
 * @description:
 */
public class PriorityThreadPoolExecutor extends ThreadPoolExecutor {

    private static ThreadLocal<Integer> priorityThreadLocal = ThreadLocal.withInitial(() -> 0);

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, getWorkQueue());
    }

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, getWorkQueue(), threadFactory);
    }

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, getWorkQueue(), handler);
    }

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, getWorkQueue(), threadFactory, handler);
    }

    public void execute(Runnable command, int priority) {
        super.execute(new PriorityRunnable(command, priority));
    }

    @Override
    public void execute(Runnable command) {
        int priority = priorityThreadLocal.get();
        try {
            this.execute(command, priority);
        } finally {
            priorityThreadLocal.remove();
        }
    }

    public Future<?> submit(Runnable task, int priority) {
        priorityThreadLocal.set(priority);
        return super.submit(task);
    }

    public <T> Future<T> submit(Callable<T> task, int priority) {
        priorityThreadLocal.set(priority);
        return super.submit(task);
    }

    private static PriorityBlockingQueue getWorkQueue() {
        return new PriorityBlockingQueue(1000);
    }
}
