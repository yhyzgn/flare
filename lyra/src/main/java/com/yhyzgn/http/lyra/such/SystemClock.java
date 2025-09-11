package com.yhyzgn.http.lyra.such;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 系统时钟类
 * <p>
 * Created on 2025-09-10 16:37
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class SystemClock {
    private final static String THREAD_NAME = "lyra.system.clock";
    private final static SystemClock INSTANCE = new SystemClock(1);

    private final long period;
    private final AtomicLong now;
    private final ThreadFactory factory;

    private SystemClock(long period) {
        this.period = period;
        this.now = new AtomicLong(System.currentTimeMillis());
        this.factory = Thread.ofVirtual().name(THREAD_NAME, 0).factory();

        schedule();
    }

    public static long now() {
        return INSTANCE.now.get();
    }

    public static Date nowDate() {
        return new Date(now());
    }

    private void schedule() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(factory);
        executor.scheduleAtFixedRate(() -> {
            now.set(System.currentTimeMillis());
        }, period, period, TimeUnit.MILLISECONDS);
    }
}
