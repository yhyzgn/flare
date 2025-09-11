package com.yhyzgn.http.flare.such.delegate;

import com.yhyzgn.http.flare.annotation.Header;
import com.yhyzgn.http.flare.delegate.DynamicHeaderDelegate;
import com.yhyzgn.http.flare.utils.ReflectUtils;

/**
 * 通过构造方法创建动态请求头类实例的代理接口实现
 * <p>
 * Created on 2025-09-10 16:45
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConstructorDynamicHeaderDelegate implements DynamicHeaderDelegate {

    @Override
    public <T extends Header.Dynamic> T apply(Class<T> clazz) throws Exception {
        return ReflectUtils.newInstance(clazz);
    }

    /**
     * 创建实例
     *
     * @return 实例
     */
    public static ConstructorDynamicHeaderDelegate create() {
        return new ConstructorDynamicHeaderDelegate();
    }
}
