package com.asuala.file.server.enums.state;

import lombok.Getter;

/**
 * @description:
 * @create: 2024/02/10
 **/
@Getter
public enum ChannelEnum {

    //     * 0 未下载  1 已下载 2 txt 3 txt已下载 4 解析下载地址失败
    UNTREATED(0,"未处理"),
    HANDLED(1,"已处理"),
    SPIDER_FAILED(2,"爬取内容失败"),
    ;

    ChannelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;
    private String desc;

    public static ChannelEnum convert(int code){
        for (ChannelEnum value : ChannelEnum.values()) {
            if (value.getCode()==code){
                return value;
            }
        }
        return UNTREATED;
    }
}