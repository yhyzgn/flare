package com.yhy.http.flare.spring.starter.annotation;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.Interceptor;
import com.yhy.http.flare.spring.convert.ObjectMapperConverterFactory;
import com.yhy.http.flare.spring.convert.SpringStringConverterFactory;
import com.yhy.http.flare.spring.delegate.SpringDispatcherProviderDelegate;
import com.yhy.http.flare.spring.delegate.SpringDynamicHeaderDelegate;
import com.yhy.http.flare.spring.delegate.SpringInterceptorDelegate;
import com.yhy.http.flare.spring.delegate.SpringMethodAnnotationDelegate;
import com.yhy.http.flare.spring.provider.SpringDispatcherProvider;
import com.yhy.http.flare.spring.starter.config.FlareStarterAutoConfiguration;
import com.yhy.http.flare.spring.starter.register.FlareAutoRegister;
import com.yhy.http.flare.such.interceptor.HttpLoggerInterceptor;
import com.yhy.http.flare.such.ssl.VoidSSLHostnameVerifier;
import com.yhy.http.flare.such.ssl.VoidSSLSocketFactory;
import com.yhy.http.flare.such.ssl.VoidSSLX509TrustManager;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.*;

/**
 * 启用 flare 组件
 * <p>
 * Created on 2025-09-18 11:11
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({
        FlareAutoRegister.class,
        FlareStarterAutoConfiguration.class,
        ObjectMapperConverterFactory.class,
        SpringStringConverterFactory.class,
        SpringDynamicHeaderDelegate.class,
        SpringInterceptorDelegate.class,
        SpringMethodAnnotationDelegate.class,
        SpringDispatcherProviderDelegate.class,
        SpringDispatcherProvider.class
})
public @interface EnableFlare {

    @AliasFor("basePackages")
    String[] value() default "";

    @AliasFor("value")
    String[] basePackages() default "";

    Class<?>[] basePackageClasses() default {};

    String baseUrl() default "";

    Header[] header() default {};

    Interceptor[] interceptor() default {};

    String timeout() default "6000";

    String logEnabled() default "true";

    Class<? extends okhttp3.Interceptor> loggerInterceptor() default HttpLoggerInterceptor.class;

    Class<? extends SSLSocketFactory> sslSocketFactory() default VoidSSLSocketFactory.class;

    Class<? extends X509TrustManager> sslTrustManager() default VoidSSLX509TrustManager.class;

    Class<? extends HostnameVerifier> sslHostnameVerifier() default VoidSSLHostnameVerifier.class;
}
