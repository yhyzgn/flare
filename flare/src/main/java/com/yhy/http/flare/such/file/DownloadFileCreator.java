package com.yhy.http.flare.such.file;

import com.yhy.http.flare.annotation.Download;
import com.yhy.http.flare.file.FileCreator;
import com.yhy.http.flare.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 指定下载文件创建器
 * <p>
 * Created on 2025-09-17 16:34
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class DownloadFileCreator implements FileCreator<Download> {
    private final TempFileCreator tempFileCreator = new TempFileCreator();

    @Override
    public File create(Download download) throws IOException {
        if (null == download) {
            return tempFileCreator.create(null);
        }
        String filePath = download.filePath();
        if (!StringUtils.hasText(filePath)) {
            log.warn("Download file path is empty, use temp file instead.");
            return tempFileCreator.create(null);
        }

        File file = new File(filePath);
        // 如果文件存在，且允许覆盖，则先删除文件
        if (download.overwrite()) {
            FileUtils.forceDelete(file);
        }
        // 创建文件
        return Files.createFile(file.toPath()).toFile();
    }
}
