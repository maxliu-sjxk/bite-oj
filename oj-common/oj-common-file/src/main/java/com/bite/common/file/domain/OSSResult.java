package com.bite.common.file.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OSSResult {

    private String name;

    /**
     * 标识对象状态，true为成功，false为失败
     */
    private boolean success;
}
