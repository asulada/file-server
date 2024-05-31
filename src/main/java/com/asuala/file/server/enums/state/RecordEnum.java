package com.asuala.file.server.enums.state;

import lombok.Getter;

/**
 * @description:
 * @create: 2024/02/10
 **/
@Getter
public enum RecordEnum {

    //     * 0 未下载  1 已下载 2 txt 3 txt已下载 4 解析下载地址失败
    UNTREATED(0,"未处理"),
    HANDLED(1,"已处理"),
    TXT(2,"txt"),
    TXT_DOWNLOADED(3,"txt已下载"),
    PARSE_DOWNLOAD_FAIL(4,"解析下载地址失败"),
    FORBID_DOWN(5,"禁止下载"),
    PAUSE_DOWN(6,"暂停下载"),
    ;

    RecordEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;
    private String desc;

    public static RecordEnum convert(int code){
        for (RecordEnum value : RecordEnum.values()) {
            if (value.getCode()==code){
                return value;
            }
        }
        return UNTREATED;
    }
}