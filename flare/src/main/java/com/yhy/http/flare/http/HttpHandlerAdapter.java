package com.yhy.http.flare.http;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.call.CallAdapter;
import com.yhy.http.flare.call.Caller;
import com.yhy.http.flare.convert.BodyConverter;
import com.yhy.http.flare.http.request.RequestFactory;
import com.yhy.http.flare.such.call.OkCaller;
import com.yhy.http.flare.utils.Assert;
import com.yhy.http.flare.utils.ReflectUtils;
import okhttp3.ResponseBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * {@link HttpHandler} 的抽象实现。
 * <p>
 * 组装 {@link RequestFactory}（请求构建）与 {@link CallAdapter}（返回类型适配），
 * 把代理方法调用转换为 OkHttp 调用。
 *
 * @param <R> 响应体转换后的类型
 * @param <T> 代理方法返回类型
 * <p>
 * Created on 2025-09-11 06:07
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class HttpHandlerAdapter<R, T> implements HttpHandler<T> {
    private final RequestFactory requestFactory;
    private final Flare flare;
    private final BodyConverter<ResponseBody, R> responseConverter;

    private HttpHandlerAdapter(RequestFactory requestFactory, Flare flare, BodyConverter<ResponseBody, R> responseConverter) {
        this.requestFactory = requestFactory;
        this.flare = flare;
        this.responseConverter = responseConverter;
    }

    /**
     * 执行调用。
     * <p>
     * 创建 OkCaller 并交给 {@link #adapt(Caller, Object[])} 完成返回类型适配。
     *
     * @param args 代理方法实参数组
     * @return 适配后的方法返回值
     * @throws Exception 请求执行或响应转换失败
     */
    @Override
    public T invoke(Object[] args) throws Exception {
        OkCaller<R> call = new OkCaller<>(requestFactory, flare, responseConverter, args);
        return adapt(call, args);
    }

    /**
     * 把 {@link Caller} 适配为代理方法的最终返回值。
     *
     * @param caller 待适配的请求调用器
     * @param args   代理方法实参数组
     * @return 适配后的方法返回值
     * @throws Exception 适配或执行请求失败
     */
    protected abstract T adapt(Caller<R> caller, Object[] args) throws Exception;

    /**
     * 解析方法注解并构建该方法的 {@link HttpHandler}。
     * <p>
     * 会先校验返回类型不含类型变量或通配符，再委托 {@link RequestFactory#parseAnnotations}。
     *
     * @param flare  当前 Flare 实例
     * @param method 接口方法
     * @return 与方法绑定的调用处理器
     */
    public static HttpHandler<?> parseAnnotations(Flare flare, Method method) {
        Type returnType = method.getGenericReturnType();
        Assert.isFalse(ReflectUtils.hasUnresolvableType(returnType), ReflectUtils.methodError(method, "Method return type must not include a type variable or wildcard: %s", returnType));
        return parseAnnotations(flare, method, RequestFactory.parseAnnotations(flare, method));
    }

    private static <R, T> HttpHandlerAdapter<R, T> parseAnnotations(Flare flare, Method method, RequestFactory factory) {
        Type returnType = method.getGenericReturnType();
        Annotation[] annotations = method.getAnnotations();
        CallAdapter<R, T> callAdapter = createCallAdapter(flare, annotations, returnType);
        Type responseType = callAdapter.responseType();
        BodyConverter<ResponseBody, R> responseConverter = createResponseConverter(flare, annotations, responseType);

        return new AdaptedCaller<>(factory, flare, responseConverter, callAdapter);
    }

    private static <R> BodyConverter<ResponseBody, R> createResponseConverter(Flare flare, Annotation[] annotations, Type responseType) {
        return flare.responseConverter(responseType, annotations);
    }

    @SuppressWarnings("unchecked")
    private static <R, T> CallAdapter<R, T> createCallAdapter(Flare flare, Annotation[] annotations, Type returnType) {
        return (CallAdapter<R, T>) flare.callAdapter(returnType, annotations);
    }

    /**
     * 绑定了 {@link CallAdapter} 的标准实现。
     * <p>
     * 把调用器的结果原样交给适配器转换。
     *
     * @param <R> 响应体转换后的类型
     * @param <T> 代理方法返回类型
     */
    public static class AdaptedCaller<R, T> extends HttpHandlerAdapter<R, T> {
        private final CallAdapter<R, T> callAdapter;

        AdaptedCaller(RequestFactory requestFactory, Flare flare, BodyConverter<ResponseBody, R> responseConverter, CallAdapter<R, T> callAdapter) {
            super(requestFactory, flare, responseConverter);
            this.callAdapter = callAdapter;
        }

        /**
         * 适配调用。
         *
         * @param call 待适配的请求调用器
         * @param args 代理方法实参数组
         * @return 适配后的方法返回值
         * @throws Exception 适配或执行请求失败
         */
        @Override
        protected T adapt(Caller<R> call, Object[] args) throws Exception {
            return callAdapter.adapt(call, args);
        }
    }
}
