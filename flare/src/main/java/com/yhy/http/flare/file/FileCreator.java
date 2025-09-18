package com.yhy.http.flare.file;

import com.yhy.http.flare.convert.StringConverter;

import java.io.File;
import java.io.IOException;

/**
 * 文件创建接口
 * <p>
 * Created on 2025-09-17 16:32
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface FileCreator<T> {

    /**
     * 创建文件
     *
     * @param t               要创建的文件的相关信息
     * @param stringConverter 字符串转换器
     * @return 创建的文件
     * @throws IOException 创建文件失败
     */
    File create(T t, StringConverter<String> stringConverter) throws IOException;
}
