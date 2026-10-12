package com.yhy.http.flare.such.provider;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.yhy.http.flare.provider.DispatcherProvider;
import okhttp3.Dispatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于虚拟线程的 {@link DispatcherProvider} 默认实现。
 * <p>
 * 使用 {@code Thread.ofVirtual()}（线程名前缀 {@code flare-vt-}）的每任务一线程执行器，
 * 并经 TTL（TransmittableThreadLocal）包装以保证上下文在异步任务间正确传递。
 * <p>
 * Created on 2025-09-23 10:46
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class VirtualThreadDispatcherProvider implements DispatcherProvider {

    /**
     * 提供基于虚拟线程的 OkHttp {@link Dispatcher}。
     *
     * @return 使用 TTL 包装的虚拟线程分发器
     */
    @Override
    public Dispatcher provide() {
        ExecutorService executorService = TtlExecutors.getTtlExecutorService(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("flare-vt-", 0).factory()));
        return new Dispatcher(executorService);
    }
}
