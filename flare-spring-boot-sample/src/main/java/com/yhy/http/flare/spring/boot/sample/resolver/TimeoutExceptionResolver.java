package com.yhy.http.flare.spring.boot.sample.resolver;

import com.yhy.http.flare.annotation.exception.Catcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 超时异常转换为运行时异常
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

    @Override
    public void resolve(Throwable throwable) throws Throwable {
        log.error("超时异常处理", throwable);
    }
}
