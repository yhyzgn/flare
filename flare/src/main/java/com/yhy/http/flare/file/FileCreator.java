package com.yhy.http.flare.file;

import com.yhy.http.flare.convert.StringConverter;

import java.io.File;
import java.io.IOException;

/**
 * 文件创建接口。
 * <p>
 * 定义下载响应体落盘为 {@link File} 的策略，默认实现包括指定路径写入与临时文件写入
 * （见 {@code DownloadFileCreator} / {@code TempFileCreator}）。
 *
 * @param <T> 下载配置信息类型（如 {@code @Download} 注解）
 * <p>
 * Created on 2025-09-17 16:32
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface FileCreator<T> {

    /**
     * 创建文件。
     *
     * @param t               下载配置信息（路径、覆盖策略等）
     * @param stringConverter 字符串转换器，用于解析路径中的占位符
     * @return 创建的文件对象
     * @throws IOException 创建或写入文件失败
     */
    File create(T t, StringConverter<String> stringConverter) throws IOException;
}
