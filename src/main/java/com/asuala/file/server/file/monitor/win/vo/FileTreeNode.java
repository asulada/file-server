package com.asuala.file.server.file.monitor.win.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @create: 2024/01/31
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTreeNode {
    //文件参照号
    private long FileReferenceNumber;
    //，父文件参照号，以及文件名
    private long ParentFileReferenceNumber;
    private String FileName;

}