package com.yhy.http.flare.spring.boot.sample.header;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.model.HttpHeader;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 全局动态请求头实现
 * <p>
 * Created on 2025-09-18 14:21
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class GlobalDynamicHeader implements Header.Dynamic {

    @Override
    public HttpHeader header(Method method) {
        return HttpHeader.of("Global-Dynamic-Header", RandomStringUtils.secure().nextPrint(32));
    }
}
