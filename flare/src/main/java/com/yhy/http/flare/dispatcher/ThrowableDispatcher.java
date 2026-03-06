package com.yhy.http.flare.dispatcher;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yhy.http.flare.annotation.exception.Catcher;
import com.yhy.http.flare.delegate.ExceptionResolverDelegate;
import com.yhy.http.flare.utils.Opt;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * 异常手动分发器
 * <p>
 * Created on 2026-03-05 10:51
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class ThrowableDispatcher {

    /**
     * Caffeine 缓存：
     * key   = Flare 类全限定名 + 方法名 + 异常类全限定名
     * value = 解析后的 resolver（可能为空）
     */
    private static final Cache<String, Opt<? extends Catcher.Resolver>> CACHE = Caffeine.newBuilder()
        .maximumSize(1000)
        .weakKeys() // 弱引用键，防止内存泄漏
        .build();

    /**
     * 手动分发异常
     * <p>
     * 1. 查找缓存中是否有解析后的 resolver
     * 2. 如果有，调用 handler 处理异常
     * 3. 如果没有，查找最佳匹配的异常处理类
     * 4. 如果有，调用异常解析器委托，解析出具体的异常处理类，调用其 resolve 方法处理异常
     * 5. 如果没有，返回 false
     *
     * @param method   方法
     * @param catchers 异常处理类列表
     * @param ex       异常实例
     * @param delegate 异常解析器委托
     * @return 是否成功处理异常
     */
    public static boolean dispatch(Method method, List<Catcher> catchers, Throwable ex, ExceptionResolverDelegate delegate) {
        Class<?> exceptionType = ex.getClass();
        String cacheKey = cacheKey(method, exceptionType);

        Opt<? extends Catcher.Resolver> optResolver = CACHE.get(cacheKey, key -> mapResolverCache(catchers, ex, delegate));
        if (optResolver.isValid()) {
            try {
                optResolver.get().resolve(ex);
            } catch (Throwable handlerEx) {
                throw new RuntimeException("Exception handler execution failed", handlerEx);
            }
            return true;
        }
        // 未匹配策略（可根据需要改为透传）
        return false;
    }

    /**
     * 解析异常处理类
     * <p>
     * 1. 查找最佳匹配的异常处理类
     * 2. 调用异常解析器委托，解析出具体的异常处理类
     *
     * @param catchers 异常处理类列表
     * @param ex       异常实例
     * @param delegate 异常解析器委托
     * @return 解析后的异常处理类（可能为空）
     */
    private static Opt<? extends Catcher.Resolver> mapResolverCache(List<Catcher> catchers, Throwable ex, ExceptionResolverDelegate delegate) {
        return findBestMatch(catchers, ex).map(type -> {
            try {
                return delegate.apply(type.resolver());
            } catch (Exception e) {
                throw new RuntimeException("Exception resolving " + type, e);
            }
        });
    }

    /**
     * 查找最佳匹配的异常处理类
     * <p>
     * 1. 过滤出能够捕获该异常的类（即该异常的类或其父类）
     * 2. 在所有能匹配的类中，找出最具体的那个（子类最小）
     *
     * @param catchers 异常处理类列表
     * @param ex       异常实例
     * @return 最佳匹配的异常处理类（可能为空）
     */
    private static Opt<Catcher> findBestMatch(List<Catcher> catchers, Throwable ex) {
        Optional<Catcher> minOpt = catchers.stream()
            // 1. 过滤出能够捕获该异常的类（即该异常的类或其父类）
            .filter(type -> type.throwable().isInstance(ex))
            // 2. 在所有能匹配的类中，找出最具体的那个（子类最小）
            .min((c1, c2) -> {
                if (c1.throwable().equals(c2.throwable())) return 0;
                if (c1.throwable().isAssignableFrom(c2.throwable())) return 1;  // c2 是子类，c2 优先级高（排前面）
                if (c2.throwable().isAssignableFrom(c1.throwable())) return -1; // c1 是子类，c1 优先级高（排前面）
                return 0;
            });
        return Opt.ofNullable(minOpt.orElse(null));
    }

    /**
     * 生成缓存键
     * <p>
     * 形式：类名#方法名(参数类型1,参数类型2,...):返回值类型!异常类型
     *
     * @param method        方法
     * @param exceptionType 异常类型
     * @return 缓存键
     */
    private static String cacheKey(Method method, Class<?> exceptionType) {
        StringBuilder sb = new StringBuilder(256);
        // 类名
        sb.append(method.getDeclaringClass().getName());
        // 方法名
        sb.append("#").append(method.getName());
        // 参数
        sb.append("(");
        Class<?>[] params = method.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(params[i].getName());
        }
        sb.append(")");
        // 返回值
        sb.append(":").append(method.getReturnType().getName());
        // 异常类型
        sb.append("!").append(exceptionType.getName());
        return sb.toString();
    }
}
