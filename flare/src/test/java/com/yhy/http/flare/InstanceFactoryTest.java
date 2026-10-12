package com.yhy.http.flare;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.method.Get;
import com.yhy.http.flare.delegate.InstanceFactory;
import com.yhy.http.flare.delegate.DynamicHeaderDelegate;
import okhttp3.Interceptor;
import com.yhy.http.flare.model.HttpHeader;
import com.yhy.http.flare.such.ssl.VoidSSLHostnameVerifier;
import com.yhy.http.flare.such.ssl.VoidSSLSocketFactory;
import com.yhy.http.flare.such.ssl.VoidSSLX509TrustManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Assertions;

/**
 * 统一实例创建工厂（{@code InstanceFactory}）测试。
 * <p>
 * 验证自定义统一工厂在请求解析与 Builder 中的实际生效、旧 Delegate 配置的兼容路径、
 * 旧 Delegate getter 与统一工厂的双向桥接，以及默认 trust-all SSL 组件与部分 SSL 配置快速失败。
 *
 * @author Neo
 * @version 1.0.0
 * @since 2.0.2
 */
class InstanceFactoryTest {

    @Test
    void customFactoryIsUsedByRequestParsingAndBuilder() throws Exception {
        InstanceFactory factory = type -> newInstance(type);
        Flare flare = new Flare.Builder()
            .baseUrl("http://localhost:8080")
            .instanceFactory(factory)
            .build();

        Method method = TestApi.class.getDeclaredMethod("get");
        okhttp3.Request request = com.yhy.http.flare.http.request.RequestFactory
            .parseAnnotations(flare, method)
            .create(new okhttp3.OkHttpClient.Builder(), new Object[0]);

        Assertions.assertSame(factory, flare.instanceFactory());
        Assertions.assertEquals("factory", request.header("X-Factory"));
    }

    @Test
    void deprecatedDynamicHeaderDelegateRemainsCompatible() throws Exception {
        DynamicHeaderDelegate delegate = new DynamicHeaderDelegate() {
            @Override
            public <T extends Header.Dynamic> T apply(Class<T> clazz) {
                return newInstance(clazz);
            }
        };
        Flare flare = new Flare.Builder()
            .baseUrl("http://localhost:8080")
            .dynamicHeaderDelegate(delegate)
            .build();

        Method method = TestApi.class.getDeclaredMethod("get");
        okhttp3.Request request = com.yhy.http.flare.http.request.RequestFactory
            .parseAnnotations(flare, method)
            .create(new okhttp3.OkHttpClient.Builder(), new Object[0]);

        Assertions.assertEquals("factory", request.header("X-Factory"));
        Assertions.assertEquals(FactoryHeader.class, flare.instanceFactory().createTyped(FactoryHeader.class).getClass());
    }

    @Test
    void deprecatedDelegatesBridgeToExplicitInstanceFactory() throws Exception {
        FactoryHeader header = new FactoryHeader();
        FactoryInterceptor interceptor = new FactoryInterceptor();
        InstanceFactory factory = type -> {
            if (type == FactoryHeader.class) {
                return header;
            }
            if (type == FactoryInterceptor.class) {
                return interceptor;
            }
            return newInstance(type);
        };
        Flare flare = new Flare.Builder()
            .baseUrl("http://localhost:8080")
            .instanceFactory(factory)
            .build();

        Assertions.assertSame(header, flare.headerDelegate().apply(FactoryHeader.class));
        Assertions.assertSame(interceptor, flare.interceptorDelegate().apply(FactoryInterceptor.class));
        Assertions.assertSame(header, flare.instanceFactory().createTyped(FactoryHeader.class));
        Assertions.assertSame(interceptor, flare.instanceFactory().createTyped(FactoryInterceptor.class));
    }

    @Test
    void defaultSslConfigurationUsesTrustAllComponents() {
        Flare flare = new Flare.Builder()
            .baseUrl("https://localhost:8443")
            .build();

        Assertions.assertInstanceOf(VoidSSLSocketFactory.class, flare.sslSocketFactory().get());
        Assertions.assertInstanceOf(VoidSSLX509TrustManager.class, flare.sslTrustManager().get());
        Assertions.assertInstanceOf(VoidSSLHostnameVerifier.class, flare.sslHostnameVerifier().get());
        Assertions.assertTrue(new VoidSSLHostnameVerifier().verify("any-host", null));
        Assertions.assertTrue(new VoidSSLSocketFactory().getDefaultCipherSuites().length > 0);
    }

    @Test
    void partialSslConfigurationFailsFast() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Flare.Builder()
            .baseUrl("https://localhost:8443")
            .https(new VoidSSLSocketFactory(), new VoidSSLX509TrustManager(), null)
            .build());
    }

    private static <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private interface TestApi {

        @Get("/index")
        @Header(dynamic = FactoryHeader.class)
        String get();
    }

    public static class FactoryHeader implements Header.Dynamic {
        @Override
        public HttpHeader header(Method method) {
            return HttpHeader.of("X-Factory", "factory");
        }
    }

    public static class FactoryInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
            return chain.proceed(chain.request());
        }
    }
}
