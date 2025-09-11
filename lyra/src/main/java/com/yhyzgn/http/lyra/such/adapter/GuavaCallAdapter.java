package com.yhyzgn.http.lyra.such.adapter;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.yhyzgn.http.lyra.Lyra;
import com.yhyzgn.http.lyra.call.CallAdapter;
import com.yhyzgn.http.lyra.call.Callback;
import com.yhyzgn.http.lyra.call.Caller;
import com.yhyzgn.http.lyra.exception.HttpException;
import com.yhyzgn.http.lyra.model.InternalResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 基于 Guava 的 CallAdapter 工厂
 * <p>
 * Created on 2025-09-11 09:17
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class GuavaCallAdapter implements CallAdapter.Factory {

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Lyra lyra) {
        if (getRawType(returnType) != InternalResponse.class) {
            return new BodyCallAdapter<>(returnType);
        }
        Type responseType = getFirstParameterUpperBound((ParameterizedType) returnType);
        return new ResponseCallAdapter<>(responseType);
    }

    private record BodyCallAdapter<R>(Type returnType) implements CallAdapter<R, R> {

        @Override
        public R adapt(Caller<R> caller, Object[] args) throws Exception {
            ListenableFuture<R> future = new AbstractFuture<>() {
                {
                    caller.enqueue(new Callback<>() {
                        @Override
                        public void onResponse(Caller<R> caller, InternalResponse<R> response) {
                            if (response.isSuccessful()) {
                                set(response.body());
                            } else {
                                setException(new HttpException(response));
                            }
                        }

                        @Override
                        public void onFailure(Caller<R> caller, Throwable t) {
                            setException(t);
                        }
                    });
                }

                @Override
                protected void interruptTask() {
                    super.interruptTask();
                    caller.cancel();
                }
            };
            return future.get();
        }
    }

    private record ResponseCallAdapter<R>(Type returnType) implements CallAdapter<R, InternalResponse<R>> {

        @Override
        public InternalResponse<R> adapt(Caller<R> caller, Object[] args) throws Exception {
            ListenableFuture<InternalResponse<R>> future = new AbstractFuture<>() {
                {
                    caller.enqueue(new Callback<>() {
                        @Override
                        public void onResponse(Caller<R> call, InternalResponse<R> response) {
                            set(response);
                        }

                        @Override
                        public void onFailure(Caller<R> call, Throwable t) {
                            setException(t);
                        }
                    });
                }

                @Override
                protected void interruptTask() {
                    super.interruptTask();
                    caller.cancel();
                }
            };
            return future.get();
        }
    }
}
