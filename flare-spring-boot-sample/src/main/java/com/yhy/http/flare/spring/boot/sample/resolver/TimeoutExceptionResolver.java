package com.yhy.http.flare.spring.boot.sample.resolver;

import com.yhy.http.flare.annotation.exception.Catcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 超时异常处理器示例，在 {@code @Catcher} 中声明，记录超时异常日志。
 * <p>
 * Created on 2026-03-04 17:14
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class TimeoutExceptionResolver implements Catcher.Resolver {

    /**
     * 处理捕获的超时异常，记录错误日志后正常返回。
     *
     * @param throwable 被捕获的异常
     * @throws Throwable 处理过程中抛出的异常
     */
    @Override
    public void resolve(Throwable throwable) throws Throwable {
        log.error("超时异常处理", throwable);
    }
}
