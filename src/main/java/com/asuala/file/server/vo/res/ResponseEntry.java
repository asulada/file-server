package com.asuala.file.server.vo.res;

import org.springframework.http.ResponseEntity;

/**
 * @description:返回参数
 * @author: zhy
 * @create: 2020/05/13
 **/
public class ResponseEntry {

    public static <T> ResponseEntity ok(T data) {
        return ResponseEntity.ok().body(new Message(200, data));
    }

    public static ResponseEntity ok() {
        return ResponseEntity.ok(new Message(200, null));
    }

    public static ResponseEntity error(String message) {
        return ResponseEntity.ok(new Message(2333, message));
    }

    public static ResponseEntity errorPower() {
        return ResponseEntity.ok(new Message(2333, "权限异常"));
    }
}