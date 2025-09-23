package com.yhy.http.flare.such.provider;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.yhy.http.flare.provider.DispatcherProvider;
import okhttp3.Dispatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 虚拟线程实现并用 TTL 包装的 Dispatcher
 * <p>
 * Created on 2025-09-23 10:46
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class VirtualThreadDispatcherProvider implements DispatcherProvider {

    @Override
    public Dispatcher provide() {
        ExecutorService executorService = TtlExecutors.getTtlExecutorService(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("flare-vt-", 0).factory()));
        return new Dispatcher(executorService);
    }
}
