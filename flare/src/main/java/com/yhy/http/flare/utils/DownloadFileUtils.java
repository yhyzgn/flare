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
 * 文件下载工具类
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
     * 写入文件。
     *
     * @param annotation 值
     * @param inputStream 输入流
     * @param stringConverter 字符串
     * @return 处理结果
     * @throws Exception 调用异常
     */
    public static File write(Download annotation, InputStream inputStream, StringConverter<String> stringConverter) throws IOException {
        File file = null != annotation ? DOWNLOAD_FILE_CREATOR.create(annotation, stringConverter) : TEMP_FILE_CREATOR.create(null, stringConverter);
        FileUtils.copyInputStreamToFile(inputStream, file);
        return file;
    }
}
