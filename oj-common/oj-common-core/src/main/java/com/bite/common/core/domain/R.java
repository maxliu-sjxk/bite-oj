package com.bite.common.core.domain;


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
}
