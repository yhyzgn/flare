package com.yhy.http.flare.spring.boot.sample.header;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.model.HttpHeader;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * POST 接口级动态请求头实现，为每个 POST 请求生成随机请求头值。
 * <p>
 * Created on 2025-09-18 14:21
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class PostDynamicHeader implements Header.Dynamic {

    /**
     * 生成请求头。
     *
     * @param method 被调用的远端接口方法
     * @return 生成的动态请求头
     */
    @Override
    public HttpHeader header(Method method) {
        return HttpHeader.of("Post-Dynamic-Header", "POST - " + RandomStringUtils.secure().nextPrint(32));
    }
}
