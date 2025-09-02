package com.bite.common.core.domain;


import com.bite.common.core.enums.ResultCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class R<T> {

    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息描述
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

    public static <T> R<T> ok() {
        return assembleResult(null, ResultCode.SUCCESS);
    }

    public static <T> R<T> ok(T data) {
        return assembleResult(data, ResultCode.SUCCESS);
    }

    public static <T> R<T> fail() {
        return assembleResult(null, ResultCode.FAILED);
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return assembleResult(null, resultCode);
    }

    public static <T> R<T> fail(int code, String msg) {
        return assembleResult(code, msg, null);
    }

    private static <T> R<T> assembleResult(int code, String msg, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    private static <T> R<T> assembleResult(T data, ResultCode resultCode) {
        R<T> r = new R<>();
        r.setCode(resultCode.getCode());
        r.setMsg(resultCode.getMsg());
        r.setData(data);
        return r;
    }
}
