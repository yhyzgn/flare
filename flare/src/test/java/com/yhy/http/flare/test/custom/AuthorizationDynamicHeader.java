package com.yhy.http.flare.test.custom;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.model.HttpHeader;
import org.apache.commons.lang3.RandomStringUtils;

import java.lang.reflect.Method;

/**
 * 用于认证的动态请求头
 * <p>
 * Created on 2025-09-15 17:42
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class AuthorizationDynamicHeader implements Header.Dynamic {

    @Override
    public HttpHeader header(Method method) {
        return HttpHeader.of("Authorization", "Bearer " + RandomStringUtils.secure().nextPrint(32));
    }
}
