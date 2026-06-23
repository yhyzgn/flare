package com.yhy.http.flare.such.adapter;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.yhy.http.flare.Flare;
import com.yhy.http.flare.call.CallAdapter;
import com.yhy.http.flare.call.Callback;
import com.yhy.http.flare.call.Caller;
import com.yhy.http.flare.exception.HttpException;
import com.yhy.http.flare.model.InternalResponse;

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

    /**
     * get。
     *
     * @param returnType 值
     * @param annotations 注解
     * @param flare 值
     * @return 处理结果
     */
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Flare flare) {
        if (getRawType(returnType) != InternalResponse.class) {
            return new BodyCallAdapter<>(returnType, flare);
        }
        Type responseType = getFirstParameterUpperBound((ParameterizedType) returnType);
        return new ResponseCallAdapter<>(responseType);
    }

    private record BodyCallAdapter<R>(Type responseType, Flare flare) implements CallAdapter<R, R> {

        /**
         * 适配调用。
         *
         * @param caller 值
         * @param args 对象
         * @return 处理结果
         * @throws Exception 调用异常
         */
        @Override
        public R adapt(Caller<R> caller, Object[] args) throws Exception {
            ListenableFuture<R> future = new AbstractFuture<>() {
                {
                    caller.enqueue(new Callback<>() {
                        /**
                         * 处理响应。
                         *
                         * @param caller 值
                         * @param response 值
                         */
                        @Override
                        public void onResponse(Caller<R> caller, InternalResponse<R> response) {
                            if (flare.ignoreHttpStatus() || response.isSuccessful()) {
                                set(response.body());
                                return;
                            }
                            setException(new HttpException(response));
                        }

                        /**
                         * 处理失败。
                         *
                         * @param caller 值
                         * @param t 异常
                         */
                        @Override
                        public void onFailure(Caller<R> caller, Throwable t) {
                            setException(t);
                        }
                    });
                }

                /**
                 * 中断任务。
                 *
                 */
                @Override
                protected void interruptTask() {
                    super.interruptTask();
                    caller.cancel();
                }
            };
            return future.get();
        }
    }

    private record ResponseCallAdapter<R>(Type responseType) implements CallAdapter<R, InternalResponse<R>> {

        /**
         * 适配调用。
         *
         * @param caller 值
         * @param args 对象
         * @return 处理结果
         * @throws Exception 调用异常
         */
        @Override
        public InternalResponse<R> adapt(Caller<R> caller, Object[] args) throws Exception {
            ListenableFuture<InternalResponse<R>> future = new AbstractFuture<>() {
                {
                    caller.enqueue(new Callback<>() {
                        /**
                         * 处理响应。
                         *
                         * @param call 值
                         * @param response 值
                         */
                        @Override
                        public void onResponse(Caller<R> call, InternalResponse<R> response) {
                            set(response);
                        }

                        /**
                         * 处理失败。
                         *
                         * @param call 值
                         * @param t 异常
                         */
                        @Override
                        public void onFailure(Caller<R> call, Throwable t) {
                            setException(t);
                        }
                    });
                }

                /**
                 * 中断任务。
                 *
                 */
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
