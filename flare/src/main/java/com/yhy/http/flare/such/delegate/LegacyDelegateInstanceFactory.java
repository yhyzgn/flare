package com.yhy.http.flare.such.delegate;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.exception.Catcher;
import com.yhy.http.flare.delegate.DispatcherProviderDelegate;
import com.yhy.http.flare.delegate.DynamicHeaderDelegate;
import com.yhy.http.flare.delegate.ExceptionResolverDelegate;
import com.yhy.http.flare.delegate.InstanceFactory;
import com.yhy.http.flare.delegate.InterceptorDelegate;
import com.yhy.http.flare.provider.DispatcherProvider;
import okhttp3.Interceptor;

/**
 * 把旧版按对象类型拆分的 Delegate 配置桥接到统一 {@link InstanceFactory} 的兼容适配器。
 * <p>
 * 按目标类型路由：动态请求头、拦截器、异常处理器、请求分发器提供者分别转发给对应的旧 Delegate；
 * 未匹配或未配置旧 Delegate 时回退到 {@code fallback} 工厂（默认 {@link ConstructorInstanceFactory}）。
 * 旧 Delegate 调用失败时统一转为 {@code IllegalArgumentException}。
 * <p>
 * Created on 2026-10-12 14:25
 *
 * @author 颜洪毅
 * @version 2.0.2
 * @since 2.0.2
 */
public final class LegacyDelegateInstanceFactory implements InstanceFactory {
    private final DynamicHeaderDelegate dynamicHeaderDelegate;
    private final InterceptorDelegate interceptorDelegate;
    private final ExceptionResolverDelegate exceptionResolverDelegate;
    private final DispatcherProviderDelegate dispatcherProviderDelegate;
    private final InstanceFactory fallback;

    /**
     * 以默认构造方法工厂作为回退创建兼容适配器。
     *
     * @param dynamicHeaderDelegate      动态请求头旧 Delegate，可为 {@code null}
     * @param interceptorDelegate        拦截器旧 Delegate，可为 {@code null}
     * @param exceptionResolverDelegate  异常处理器旧 Delegate，可为 {@code null}
     * @param dispatcherProviderDelegate 请求分发器提供者旧 Delegate，可为 {@code null}
     */
    public LegacyDelegateInstanceFactory(
        DynamicHeaderDelegate dynamicHeaderDelegate,
        InterceptorDelegate interceptorDelegate,
        ExceptionResolverDelegate exceptionResolverDelegate,
        DispatcherProviderDelegate dispatcherProviderDelegate
    ) {
        this(dynamicHeaderDelegate, interceptorDelegate, exceptionResolverDelegate, dispatcherProviderDelegate, ConstructorInstanceFactory.create());
    }

    /**
     * 以显式回退工厂创建兼容适配器。
     *
     * @param dynamicHeaderDelegate      动态请求头旧 Delegate，可为 {@code null}
     * @param interceptorDelegate        拦截器旧 Delegate，可为 {@code null}
     * @param exceptionResolverDelegate  异常处理器旧 Delegate，可为 {@code null}
     * @param dispatcherProviderDelegate 请求分发器提供者旧 Delegate，可为 {@code null}
     * @param fallback                   未命中旧 Delegate 时使用的回退工厂
     */
    public LegacyDelegateInstanceFactory(
        DynamicHeaderDelegate dynamicHeaderDelegate,
        InterceptorDelegate interceptorDelegate,
        ExceptionResolverDelegate exceptionResolverDelegate,
        DispatcherProviderDelegate dispatcherProviderDelegate,
        InstanceFactory fallback
    ) {
        this.dynamicHeaderDelegate = dynamicHeaderDelegate;
        this.interceptorDelegate = interceptorDelegate;
        this.exceptionResolverDelegate = exceptionResolverDelegate;
        this.dispatcherProviderDelegate = dispatcherProviderDelegate;
        this.fallback = fallback;
    }

    /**
     * 按目标类型路由到旧 Delegate 创建实例，未命中时回退到 {@code fallback}。
     *
     * @param type 目标类型
     * @return 旧 Delegate 或回退工厂创建的实例
     * @throws IllegalArgumentException 旧 Delegate 创建实例失败时抛出
     */
    @Override
    public Object create(Class<?> type) {
        try {
            if (dynamicHeaderDelegate != null && Header.Dynamic.class.isAssignableFrom(type)) {
                return dynamicHeaderDelegate.apply(type.asSubclass(Header.Dynamic.class));
            }
            if (interceptorDelegate != null && Interceptor.class.isAssignableFrom(type)) {
                return interceptorDelegate.apply(type.asSubclass(Interceptor.class));
            }
            if (exceptionResolverDelegate != null && Catcher.Resolver.class.isAssignableFrom(type)) {
                return exceptionResolverDelegate.apply(type.asSubclass(Catcher.Resolver.class));
            }
            if (dispatcherProviderDelegate != null && DispatcherProvider.class.isAssignableFrom(type)) {
                return dispatcherProviderDelegate.apply(type.asSubclass(DispatcherProvider.class));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create instance: " + type.getName(), e);
        }
        return fallback.create(type);
    }
}
