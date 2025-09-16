package com.yhy.http.flare.mock.server.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

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

    private MultipartFile file;

    private MultipartFile bytesFile;

    private MultipartFile inputStreamFile;
}
