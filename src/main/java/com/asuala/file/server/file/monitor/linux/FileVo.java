package com.asuala.file.server.file.monitor.linux;

import lombok.Data;

/**
 * @description:
 * @create: 2024/05/10
 **/
@Data
public class FileVo {
    private String fullPath;
    private String parentPath;
    private String name;
    private int code;
    private boolean isDir;
    private Long sId;

    private Integer fd;
}