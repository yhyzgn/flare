package com.yhy.http.flare.such.file;

import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.file.FileCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 在系统临时目录创建下载目标文件的 {@link FileCreator} 实现。
 * <p>
 * 文件名形如 {@code flare-download-*.tmp}，由 {@code Files.createTempFile} 保证唯一。
 * <p>
 * Created on 2025-09-17 16:34
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class TempFileCreator implements FileCreator<Void> {

    /**
     * 创建临时下载文件。
     *
     * @param unused          未使用，仅为满足 {@link FileCreator} 接口签名
     * @param stringConverter 未使用，仅为满足 {@link FileCreator} 接口签名
     * @return 新创建的临时文件
     * @throws IOException 创建临时文件失败时抛出
     */
    @Override
    public File create(Void unused, StringConverter<String> stringConverter) throws IOException {
        return Files.createTempFile("flare-download-", ".tmp").toFile();
    }
}
