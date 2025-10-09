package com.yhy.http.flare.model;

/**
 * 一些内置常量
 * <p>
 * Created on 2025-10-09 14:52
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface FlareConst {

    interface ContentType {
        String APPLICATION_JSON = "application/json";

        String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

        String MULTIPART_FORM_DATA = "multipart/form-data";
    }

    interface Timeout {

        long DEFAULT_MILLIS = 6000;
    }
}
