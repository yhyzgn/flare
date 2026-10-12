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
 * 兼容旧版按对象类型拆分的 Delegate 配置。
 *
 * @since 2.0.2
 */
public final class LegacyDelegateInstanceFactory implements InstanceFactory {
    private final DynamicHeaderDelegate dynamicHeaderDelegate;
    private final InterceptorDelegate interceptorDelegate;
    private final ExceptionResolverDelegate exceptionResolverDelegate;
    private final DispatcherProviderDelegate dispatcherProviderDelegate;
    private final InstanceFactory fallback;

    public LegacyDelegateInstanceFactory(
        DynamicHeaderDelegate dynamicHeaderDelegate,
        InterceptorDelegate interceptorDelegate,
        ExceptionResolverDelegate exceptionResolverDelegate,
        DispatcherProviderDelegate dispatcherProviderDelegate
    ) {
        this(dynamicHeaderDelegate, interceptorDelegate, exceptionResolverDelegate, dispatcherProviderDelegate, ConstructorInstanceFactory.create());
    }

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
