package com.yhy.http.flare.test.model;

import com.yhy.http.flare.annotation.param.Multipart;
import lombok.Data;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serial;
import java.io.Serializable;

/**
 *
 * <p>
 * Created on 2025-09-16 16:53
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class PartForm implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    @Multipart
    private File file;

    @Multipart(filename = "bytes.webp")
    private byte[] bytesFile;

    @Multipart(value = "inputStreamFile", filename = "tempInputStreamFile.webp")
    private FileInputStream tempInputStreamFile;
}
