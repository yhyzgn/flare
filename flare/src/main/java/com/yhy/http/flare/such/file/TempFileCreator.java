package com.yhy.http.flare.such.file;

import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.file.FileCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 临时下载文件创建器
 * <p>
 * Created on 2025-09-17 16:34
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class TempFileCreator implements FileCreator<Void> {

    @Override
    public File create(Void unused, StringConverter<String> stringConverter) throws IOException {
        return Files.createTempFile("flare-download-", ".tmp").toFile();
    }
}
