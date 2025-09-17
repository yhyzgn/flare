package com.yhy.http.flare.utils;

import com.yhy.http.flare.annotation.Download;
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

    public static File write(Download annotation, InputStream inputStream) throws IOException {
        File file = null != annotation ? DOWNLOAD_FILE_CREATOR.create(annotation) : TEMP_FILE_CREATOR.create(null);
        FileUtils.copyInputStreamToFile(inputStream, file);
        return file;
    }
}
