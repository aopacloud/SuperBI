package net.aopacloud.superbi.manager;

import net.aopacloud.superbi.common.core.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/1 11:00
 */
@Slf4j
public class AsyncManager {
    /**
     * 操作延迟10毫秒
     */
    private final int OPERATE_DELAY_TIME = 10;

    /**
     * 异步操作任务调度线程池
     */
    private ScheduledExecutorService executor = SpringUtils.getBean("scheduledExecutorService");

    /**
     * 单例模式
     */
    private AsyncManager(){}

    private static AsyncManager me = new AsyncManager();

    public static AsyncManager me()
    {
        return me;
    }

    /**
     * 执行任务
     *
     * @param task 任务
     */
    public void execute(TimerTask task)
    {
        executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止任务线程池
     */
    public void shutdown()
    {
        if (executor != null && !executor.isShutdown())
        {
            executor.shutdown();
            try
            {
                if (!executor.awaitTermination(120, TimeUnit.SECONDS))
                {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(120, TimeUnit.SECONDS))
                    {
                        log.info("Pool did not terminate");
                    }
                }
            }
            catch (InterruptedException ie)
            {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
