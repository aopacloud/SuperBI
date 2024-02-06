package net.aopacloud.superbi.queryEngine.schedule;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.queryEngine.executor.QueryExecutor;
import net.aopacloud.superbi.queryEngine.model.QueryContext;
import net.aopacloud.superbi.queryEngine.model.QueryResult;
import net.aopacloud.superbi.thread.PriorityThreadPoolExecutor;
import net.aopacloud.superbi.util.ThreadUtils;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Priority Scheduler.
 * The priority is determined by the number of queries that the user is currently running.
 * user running query number is less, the priority is higher.
 *
 * @author: hudong
 * @date: 2023/8/24
 * @description:
 */
@Slf4j
public class PriorityQueryScheduler implements QueryScheduler {

    private final QueryExecutor queryExecutor;

    private PriorityThreadPoolExecutor queryThreadPool;

    private static Map<String, AtomicInteger> userRunningNumMap = Maps.newHashMap();

    public PriorityQueryScheduler(QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
        ThreadFactory threadFactory = ThreadUtils.namedThreadFactory(BiConsist.QUERY_THREAD_POOL_NAME);
        queryThreadPool = new PriorityThreadPoolExecutor(BiConsist.QUERY_THREAD_NUM, BiConsist.QUERY_THREAD_NUM, 0, TimeUnit.MILLISECONDS, threadFactory);
    }

    @Override
    public QueryResult submit(QueryContext queryContext) {
        try {
            int priority = getPriority(queryContext.getUsername());

            Future<QueryResult> future = queryThreadPool.submit(() -> queryExecutor.execute(queryContext), priority);

            return future.get();
        } catch (Exception e) {
            log.error("get future result error", e);
        } finally {
            cutDownUserRunningNum(queryContext.getUsername());
        }
        return new QueryResult();
    }

    private int getPriority(String username) {
        AtomicInteger atomicInteger = userRunningNumMap.get(username);
        if (atomicInteger == null) {
            synchronized (this) {
                if (userRunningNumMap.get(username) == null) {
                    atomicInteger = new AtomicInteger(0);
                    userRunningNumMap.put(username, atomicInteger);
                } else {
                    atomicInteger = userRunningNumMap.get(username);
                }
            }
        }

        int userRunningTaskNum = atomicInteger.incrementAndGet();
        log.info("user {} current running task num {}", username, userRunningTaskNum);
        int priority = 10 - (userRunningTaskNum / 10);

        return priority < 0 ? 0 : priority;
    }

    private void cutDownUserRunningNum(String username) {
        if (!Strings.isNullOrEmpty(username)) {
            AtomicInteger atomicInteger = userRunningNumMap.get(username);
            if (atomicInteger != null) {
                atomicInteger.decrementAndGet();
            }
        }
    }

}
