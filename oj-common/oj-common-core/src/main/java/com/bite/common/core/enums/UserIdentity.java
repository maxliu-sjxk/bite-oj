package com.bite.common.core.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserIdentity {

    ORDINARY      (1, "普通用户"),
    ADMIN         (2, "管理员用户");


    private Integer value;

    private String desc;
}
