package com.asuala.file.server.enums.state;

import lombok.Getter;

/**
 * @description:
 * @create: 2024/02/10
 **/
@Getter
public enum ChannelDetailsEnum {

    //     * 0 未下载  1 已下载 2 txt 3 txt已下载 4 解析下载地址失败
    UNTREATED(0,"未处理"),
    HANDLED(1,"已处理"),
    REPEAT_RECORD(2,"和record重复"),
    REPEAT_FILE(3,"和file_info重复"),
    ;

    ChannelDetailsEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;
    private String desc;

    public static ChannelDetailsEnum convert(int code){
        for (ChannelDetailsEnum value : ChannelDetailsEnum.values()) {
            if (value.getCode()==code){
                return value;
            }
        }
        return UNTREATED;
    }
}