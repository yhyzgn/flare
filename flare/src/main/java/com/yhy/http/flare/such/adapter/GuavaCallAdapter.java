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
 * 基于 Guava 的 {@link CallAdapter.Factory} 默认实现。
 * <p>
 * 按方法返回类型选择适配策略：返回 {@link InternalResponse} 时保留完整响应包装，
 * 其余类型直接提取响应体作为返回值。
 * <p>
 * 注意：{@code adapt} 内部基于 {@link ListenableFuture} 组装结果，但最终通过 {@code future.get()} 阻塞取值，
 * 因此调用线程实际为同步等待语义。
 * <p>
 * Created on 2025-09-11 09:17
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class GuavaCallAdapter implements CallAdapter.Factory {

    /**
     * 根据方法返回类型获取调用适配器。
     * <p>
     * 返回类型为 {@link InternalResponse} 时使用 {@code ResponseCallAdapter}（保留完整响应）；
     * 其他类型使用 {@code BodyCallAdapter}（直接提取响应体）。
     *
     * @param returnType 代理方法的返回类型
     * @param annotations 方法上的注解
     * @param flare       当前 Flare 客户端实例
     * @return 与返回类型匹配的调用适配器
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
         * 执行调用并把响应体适配为方法返回值。
         * <p>
         * 异步发起请求后阻塞等待：响应成功（或启用 {@code ignoreHttpStatus}）时返回响应体；
         * HTTP 状态非 2xx 时以 {@link HttpException} 失败。
         *
         * @param caller 调用执行器
         * @param args   代理方法参数
         * @return 转换后的响应体
         * @throws Exception 请求失败、HTTP 非 2xx 或等待被中断时抛出
         */
        @Override
        public R adapt(Caller<R> caller, Object[] args) throws Exception {
            ListenableFuture<R> future = new AbstractFuture<>() {
                {
                    caller.enqueue(new Callback<>() {
                        /**
                         * 响应到达时完成 Future。
                         * <p>
                         * 响应成功或忽略 HTTP 状态检查时写入响应体，否则以 {@link HttpException} 失败。
                         *
                         * @param caller   调用执行器
                         * @param response 原始响应包装
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
                         * 请求失败时以原异常使 Future 失败。
                         *
                         * @param caller 调用执行器
                         * @param t      失败原因
                         */
                        @Override
                        public void onFailure(Caller<R> caller, Throwable t) {
                            setException(t);
                        }
                    });
                }

                /**
                 * Future 被取消或中断时联动取消底层调用。
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
         * 执行调用并返回完整的 {@link InternalResponse} 包装。
         * <p>
         * 异步发起请求后阻塞等待完整响应；HTTP 状态与异常均由调用方自行处理。
         *
         * @param caller 调用执行器
         * @param args   代理方法参数
         * @return 完整的响应包装
         * @throws Exception 请求失败或等待被中断时抛出
         */
        @Override
        public InternalResponse<R> adapt(Caller<R> caller, Object[] args) throws Exception {
            ListenableFuture<InternalResponse<R>> future = new AbstractFuture<>() {
                {
                    caller.enqueue(new Callback<>() {
                        /**
                         * 响应到达时完成 Future。
                         *
                         * @param call    调用执行器
                         * @param response 原始响应包装
                         */
                        @Override
                        public void onResponse(Caller<R> call, InternalResponse<R> response) {
                            set(response);
                        }

                        /**
                         * 请求失败时以原异常使 Future 失败。
                         *
                         * @param call 调用执行器
                         * @param t    失败原因
                         */
                        @Override
                        public void onFailure(Caller<R> call, Throwable t) {
                            setException(t);
                        }
                    });
                }

                /**
                 * Future 被取消或中断时联动取消底层调用。
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
