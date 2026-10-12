package com.yhy.http.flare.model;

/**
 * Flare 内置常量。
 * <p>
 * 集中定义框架内部使用的 Content-Type 字符串与默认超时时间等常量。
 * <p>
 * Created on 2025-10-09 14:52
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface FlareConst {

    /**
     * Content-Type 相关常量。
     */
    interface ContentType {
        /**
         * JSON 请求体类型。
         */
        String APPLICATION_JSON = "application/json";

        /**
         * x-www-form-urlencoded 表单类型。
         */
        String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

        /**
         * multipart 表单类型。
         */
        String MULTIPART_FORM_DATA = "multipart/form-data";
    }

    /**
     * 超时相关常量。
     */
    interface Timeout {

        /**
         * 默认超时时间（毫秒）。
         */
        long DEFAULT_MILLIS = 6000;
    }
}
