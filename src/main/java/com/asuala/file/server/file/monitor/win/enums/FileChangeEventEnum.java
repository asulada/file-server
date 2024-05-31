package com.asuala.file.server.file.monitor.win.enums;

import com.sun.jna.platform.FileMonitor;
import lombok.Getter;

/**
 * @description:
 * @create: 2024/02/01
 **/
@Getter
public enum FileChangeEventEnum {
    /**
     public static final int FILE_CREATED = 0x1;
     public static final int FILE_DELETED = 0x2;
     public static final int FILE_MODIFIED = 0x4;
     public static final int FILE_ACCESSED = 0x8;
     public static final int FILE_NAME_CHANGED_OLD = 0x10;
     public static final int FILE_NAME_CHANGED_NEW = 0x20;
    **/
    UNKNOWN(0,""),
    FILE_CREATED(FileMonitor.FILE_CREATED,"创建"),
    FILE_DELETED(FileMonitor.FILE_DELETED,"删除"),
    FILE_MODIFIED(FileMonitor.FILE_MODIFIED,"修改"),
    FILE_NAME_CHANGED_OLD(FileMonitor.FILE_NAME_CHANGED_OLD,"原名称"),
    FILE_NAME_CHANGED_NEW(FileMonitor.FILE_NAME_CHANGED_NEW,"新名称"),
    ;

    FileChangeEventEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;
    private String desc;

    public static FileChangeEventEnum convert(int code){
        for (FileChangeEventEnum value : FileChangeEventEnum.values()) {
            if (value.getCode()==code){
                return value;
            }
        }
        return UNKNOWN;
    }
}