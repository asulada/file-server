package com.asuala.file.server.file.monitor.linux;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @create: 2024/06/03
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileNode {
    private Long sId;
    private Integer fd;
}