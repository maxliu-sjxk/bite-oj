package com.bite.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserStatus {

    NORMAL(1),

    BLOCKED(0);


    private Integer value;


}
