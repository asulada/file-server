package com.asuala.file.server.vo.res;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;

/**
 * @description:
 * @create: 2024/02/06
 **/
@Getter
@Setter
public class BaseResponse {
    private int code;
    private String msg;
    private T data;

    public static BaseResponse ok(T data) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(200);
        baseResponse.setMsg("成功");
        baseResponse.setData(data);
        return baseResponse;
    }

    public static BaseResponse ok() {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(200);
        baseResponse.setMsg("成功");
        return baseResponse;
    }

    public static BaseResponse err(String msg, T data) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(222);
        baseResponse.setMsg(msg);
        baseResponse.setData(data);
        return baseResponse;
    }

    public static BaseResponse err(T data) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(222);
        baseResponse.setMsg("失败");
        baseResponse.setData(data);
        return baseResponse;
    }

    public static BaseResponse err(String msg) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(222);
        baseResponse.setMsg(msg + "为空");
        return baseResponse;
    }

    public static BaseResponse err(String... msg) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(222);
        baseResponse.setMsg(StringUtils.join(msg, " "));
        return baseResponse;
    }
}