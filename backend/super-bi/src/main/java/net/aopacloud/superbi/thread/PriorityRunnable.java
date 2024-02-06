package net.aopacloud.superbi.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: hudong
 * @date: 2022/8/17
 * @description:
 */
@Slf4j
public class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {

    private final static AtomicLong seq = new AtomicLong();

    private final long seqNum;

    private Runnable realRunnable;

    private int priority;

    public PriorityRunnable(Runnable realRunnable, int priority) {
        this.realRunnable = realRunnable;
        this.priority = priority;
        seqNum = seq.getAndIncrement();
    }

    @Override
    public int compareTo(PriorityRunnable o) {
        int res = 0;
        if (this.priority == o.priority) {
            if (o.realRunnable != this.realRunnable) {
                // ASC , 优先级相等，按 FIFO 执行任务
                res = (seqNum < o.seqNum ? -1 : 1);
            }
        } else {
            // DESC ，priority 越大，优先级越高
            res = this.priority > o.priority ? -1 : 1;
        }
        return res;
    }

    @Override
    public void run() {
        try {
            log.info("this thread priority={}, start execute ", priority);
            realRunnable.run();
        } catch (Exception e) {
            log.error("thread execute errror", e);
        }
    }
}
