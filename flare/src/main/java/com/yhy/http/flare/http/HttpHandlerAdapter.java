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
 *
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

    @Override
    public T invoke(Object[] args) throws Exception {
        OkCaller<R> call = new OkCaller<>(requestFactory, flare, responseConverter, args);
        return adapt(call, args);
    }

    protected abstract T adapt(Caller<R> caller, Object[] args) throws Exception;

    public static HttpHandler<?> parseAnnotations(Flare pigeon, Method method) {
        Type returnType = method.getGenericReturnType();
        Assert.isFalse(ReflectUtils.hasUnresolvableType(returnType), ReflectUtils.methodError(method, "Method return type must not include a type variable or wildcard: %s", returnType));
        Assert.isFalse(returnType == void.class, ReflectUtils.methodError(method, "Service methods cannot return void."));
        return parseAnnotations(pigeon, method, RequestFactory.parseAnnotations(pigeon, method));
    }

    private static <R, T> HttpHandlerAdapter<R, T> parseAnnotations(Flare pigeon, Method method, RequestFactory factory) {
        Type returnType = method.getGenericReturnType();
        Annotation[] annotations = method.getAnnotations();
        CallAdapter<R, T> callAdapter = createCallAdapter(pigeon, annotations, returnType);
        Type responseType = callAdapter.responseType();
        BodyConverter<ResponseBody, R> responseConverter = createResponseConverter(pigeon, annotations, responseType);

        return new AdaptedCaller<>(factory, pigeon, responseConverter, callAdapter);
    }

    private static <R> BodyConverter<ResponseBody, R> createResponseConverter(Flare pigeon, Annotation[] annotations, Type responseType) {
        return pigeon.responseConverter(responseType, annotations);
    }

    @SuppressWarnings("unchecked")
    private static <R, T> CallAdapter<R, T> createCallAdapter(Flare pigeon, Annotation[] annotations, Type returnType) {
        return (CallAdapter<R, T>) pigeon.callAdapter(returnType, annotations);
    }

    public static class AdaptedCaller<R, T> extends HttpHandlerAdapter<R, T> {
        private final CallAdapter<R, T> callAdapter;

        AdaptedCaller(RequestFactory requestFactory, Flare pigeon, BodyConverter<ResponseBody, R> responseConverter, CallAdapter<R, T> callAdapter) {
            super(requestFactory, pigeon, responseConverter);
            this.callAdapter = callAdapter;
        }

        @Override
        protected T adapt(Caller<R> call, Object[] args) throws Exception {
            return callAdapter.adapt(call, args);
        }
    }
}
