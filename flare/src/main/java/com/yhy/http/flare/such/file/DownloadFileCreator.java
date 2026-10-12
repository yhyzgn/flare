package com.yhy.http.flare.such.file;

import com.yhy.http.flare.annotation.Download;
import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.file.FileCreator;
import com.yhy.http.flare.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 按 {@link Download} 注解配置创建下载目标文件的 {@link FileCreator} 实现。
 * <p>
 * 未配置 {@code Download} 或路径为空时回退为创建临时文件；路径支持通过字符串转换器解析 placeholder。
 * 目标文件已存在且 {@code overwrite} 为 {@code true} 时先删除再重建。
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

    /**
     * 创建下载目标文件。
     * <p>
     * 注解为 {@code null} 或其路径为空时创建临时文件；路径经字符串转换器解析后，
     * 若文件已存在且允许覆盖则先删除，最终以 {@code Files.createFile} 建立新文件。
     *
     * @param download        下载注解配置，可为 {@code null}
     * @param stringConverter 用于解析文件路径中 placeholder 的字符串转换器
     * @return 新创建的文件
     * @throws IOException 删除旧文件或创建新文件失败时抛出
     */
    @Override
    public File create(Download download, StringConverter<String> stringConverter) throws IOException {
        if (null == download) {
            return tempFileCreator.create(null, stringConverter);
        }
        String filePath = download.filePath();
        if (!StringUtils.hasText(filePath)) {
            log.warn("Download file path is empty, use temp file instead.");
            return tempFileCreator.create(null, stringConverter);
        }

        try {
            filePath = stringConverter.convert(filePath);
        } catch (Exception e) {
            log.error("", e);
        }
        File file = new File(filePath);
        // 如果文件存在，且允许覆盖，则先删除文件
        if (file.exists() && download.overwrite()) {
            FileUtils.forceDelete(file);
        }
        // 创建文件
        return Files.createFile(file.toPath()).toFile();
    }
}
