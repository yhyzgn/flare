package com.yhy.http.flare.dispatcher;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yhy.http.flare.annotation.exception.Catcher;
import com.yhy.http.flare.delegate.InstanceFactory;
import com.yhy.http.flare.delegate.ExceptionResolverDelegate;
import com.yhy.http.flare.such.delegate.LegacyDelegateInstanceFactory;
import com.yhy.http.flare.utils.Opt;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * 异常手动分发器。
 * <p>
 * 把代理方法执行中抛出的异常分发给方法上 {@code @Catcher} 声明的 {@code Resolver} 处理器，
 * 解析结果按「方法 + 异常类型」缓存在 Caffeine 中以加速后续分发。
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
     * 分发异常到方法上声明的 {@code @Catcher} 处理器。
     * <p>
     * 流程：先查缓存中已解析的 resolver；未命中则从 {@code catchers} 中查找与异常类型
     * 最匹配的处理类，通过 {@code instanceFactory} 创建实例后回调并写入缓存；
     * 无任何匹配时返回 {@code false}，由调用方决定是否继续抛出原异常。
     *
     * @param method          代理方法，用于生成缓存键
     * @param catchers        方法上声明的异常捕获器注解列表
     * @param ex              待分发的异常实例
     * @param instanceFactory 创建 resolver 实例的统一工厂
     * @return 异常被处理器成功处理返回 {@code true}，否则 {@code false}
     */
    public static boolean dispatch(Method method, List<Catcher> catchers, Throwable ex, InstanceFactory instanceFactory) {
        Class<?> exceptionType = ex.getClass();
        String cacheKey = cacheKey(method, exceptionType);

        Opt<? extends Catcher.Resolver> optResolver = CACHE.get(cacheKey, key -> mapResolverCache(catchers, ex, instanceFactory));
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
     * 使用旧异常解析器委托分发异常。
     *
     * @param method 方法
     * @param catchers 异常处理类列表
     * @param ex 异常实例
     * @param delegate 旧异常解析器委托
     * @return 是否成功处理异常
     * @deprecated since 2.0.2; use the InstanceFactory overload instead. This API is planned for removal in 3.0.0.
     */
    @Deprecated(since = "2.0.2")
    public static boolean dispatch(Method method, List<Catcher> catchers, Throwable ex, ExceptionResolverDelegate delegate) {
        return dispatch(method, catchers, ex, new LegacyDelegateInstanceFactory(null, null, delegate, null));
    }

    /**
     * 解析异常处理类并填充缓存值。
     * <p>
     * 先查找与异常类型最匹配的 {@code @Catcher}，再通过统一工厂创建其 resolver 实例。
     *
     * @param catchers        方法上声明的异常捕获器注解列表
     * @param ex              异常实例
     * @param instanceFactory 创建 resolver 实例的统一工厂
     * @return 解析后的异常处理器，无匹配时为空 {@link Opt}
     */
    private static Opt<? extends Catcher.Resolver> mapResolverCache(List<Catcher> catchers, Throwable ex, InstanceFactory instanceFactory) {
        return findBestMatch(catchers, ex).map(type -> {
            try {
                return instanceFactory.createTyped(type.resolver());
            } catch (Exception e) {
                throw new RuntimeException("Exception resolving " + type, e);
            }
        });
    }

    /**
     * 查找能捕获该异常且类型最具体的 {@code @Catcher}。
     * <p>
     * 先过滤出 {@code throwable()} 能匹配该异常实例的注解，再在能匹配的注解中
     * 优先选择异常类型声明最具体的那个（子类优先）。
     *
     * @param catchers 异常捕获器注解列表
     * @param ex       异常实例
     * @return 最佳匹配的捕获器，无匹配时为空 {@link Opt}
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
     * 生成缓存键。
     * <p>
     * 形式：{@code 类名#方法名(参数类型1,参数类型2,...):返回值类型!异常类名}。
     *
     * @param method        代理方法
     * @param exceptionType 异常类型
     * @return 缓存键字符串
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
