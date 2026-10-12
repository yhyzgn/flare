package com.yhy.http.flare.such;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 系统时钟。
 * <p>
 * 内部以毫秒周期由虚拟线程定时任务把 {@link System#currentTimeMillis()} 缓存到 {@link AtomicLong}，
 * 读取时间戳时直接返回缓存值，避免高频调用系统时钟的开销。
 * 默认以单例方式运行，刷新周期为 1 毫秒。
 * <p>
 * Created on 2025-09-10 16:37
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class SystemClock {
    private final static String THREAD_NAME = "flare.system.clock";
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

    /**
     * 获取当前毫秒时间戳。
     * <p>
     * 返回内部缓存值，与 {@link System#currentTimeMillis()} 的偏差不超过一个刷新周期（默认 1 毫秒）。
     *
     * @return 当前毫秒时间戳
     */
    public static long now() {
        return INSTANCE.now.get();
    }

    /**
     * 获取当前日期。
     *
     * @return 以 {@link #now()} 时间戳构造的 {@link Date}
     */
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
