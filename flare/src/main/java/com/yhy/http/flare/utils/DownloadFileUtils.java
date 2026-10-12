package com.yhy.http.flare.utils;

import com.yhy.http.flare.annotation.Download;
import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.such.file.DownloadFileCreator;
import com.yhy.http.flare.such.file.TempFileCreator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件下载写盘工具类。
 * <p>
 * 与 {@link Download} 注解配合，把下载到的输入流写入目标文件；
 * 目标文件由 {@link DownloadFileCreator}（注解声明路径）或 {@link TempFileCreator}（临时目录）创建。
 * <p>
 * Created on 2025-09-17 16:47
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class DownloadFileUtils {
    private static final TempFileCreator TEMP_FILE_CREATOR = new TempFileCreator();
    private static final DownloadFileCreator DOWNLOAD_FILE_CREATOR = new DownloadFileCreator();

    private DownloadFileUtils() {
        throw new UnsupportedOperationException("Can not instantiate utils class");
    }

    /**
     * 把输入流写入下载目标文件并返回该文件。
     * <p>
     * {@code annotation} 非 {@code null} 时按注解声明的 {@code filePath} 与 {@code overwrite}
     * 规则创建目标文件；为 {@code null} 时写入系统临时目录中的新文件。
     *
     * @param annotation      方法上的 {@link Download} 注解，可为 {@code null}（表示写入临时文件）
     * @param inputStream     下载内容的输入流，由调用方负责其生命周期
     * @param stringConverter 用于解析注解中 placeholder 的字符串转换器
     * @return 写入完成的目标文件
     * @throws IOException 创建目标文件或写入流内容时发生 I/O 错误
     */
    public static File write(Download annotation, InputStream inputStream, StringConverter<String> stringConverter) throws IOException {
        File file = null != annotation ? DOWNLOAD_FILE_CREATOR.create(annotation, stringConverter) : TEMP_FILE_CREATOR.create(null, stringConverter);
        FileUtils.copyInputStreamToFile(inputStream, file);
        return file;
    }
}
