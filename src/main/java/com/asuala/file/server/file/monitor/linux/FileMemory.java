package com.asuala.file.server.file.monitor.linux;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @create: 2024/06/03
 **/
@Getter
@Setter
@Builder
public class FileMemory {
    private Long id;
    private Long dId;
}