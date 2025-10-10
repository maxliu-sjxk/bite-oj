package com.bite.system.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateStatusDTO {

    private Long userId;

    private Integer status;//要设置的状态值，即：传入0：拉黑  传入1：解禁
}
