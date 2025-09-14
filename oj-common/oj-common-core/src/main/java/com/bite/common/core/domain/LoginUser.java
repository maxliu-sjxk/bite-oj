package com.bite.common.core.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUser {
    private Integer identity; //1表示普通用户 2表示管理员用户

    private String nickName; //昵称
}
