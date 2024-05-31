package com.asuala.file.server.vo.res;

import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @create: 2020/09/04
 **/
@Setter
@Getter
public class Message {
    private Integer code;
    private Object data;

    public Message(Integer code, Object data) {
        this.code = code;
        this.data = data;
    }

}