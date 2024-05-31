package com.asuala.file.server.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @description:
 * @create: 2023/10/31
 **/
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(SaTokenException.class)
    public SaResult handlerSaTokenException(SaTokenException e) {
        log.error("请求失败 {}", e.getCode());
        // 根据不同异常细分状态码返回不同的提示
        if (e.getCode() == 30001) {
            return SaResult.error("redirect 重定向 url 是一个无效地址");
        }
        if (e.getCode() == 30002) {
            return SaResult.error("redirect 重定向 url 不在 allowUrl 允许的范围内");
        }
        if (e.getCode() == 30004) {
            return SaResult.error("提供的 ticket 是无效的");
        }
        if (e.getCode() == 11012) {
            return SaResult.code(11012);
        }
        if (e.getCode() == 11041) {
            return SaResult.error("没有权限");
        }
        if (e.getCode() == 11011) {
            log.error("token失效 请重新登录");
            return SaResult.code(11011);
        }
        // 更多 code 码判断 ...

        // 默认的提示
        return SaResult.error("服务器繁忙，请稍后重试...");
    }

    @ExceptionHandler(NotLoginException.class)
    public SaResult handlerCheckException(NotLoginException e) {
        log.error("校验token失败 {} {}", e.getCode(), e.getMessage());
        return SaResult.code(11011);
    }
}
