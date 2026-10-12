package com.yhy.http.flare.mock.server.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;

/**
 * mock 服务的多段表单模型。
 * <p>
 * 用于验证 multipart 表单中普通字段与多种来源文件分片
 * （{@code MultipartFile}）的绑定；配对端点为 {@code POST /post/partForm}。
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

    /**
     * 名称。
     */
    private String name;

    /**
     * 文件。
     */
    private MultipartFile file;

    /**
     * 字节文件。
     */
    private MultipartFile bytesFile;

    /**
     * 输入流文件。
     */
    private MultipartFile inputStreamFile;
}
