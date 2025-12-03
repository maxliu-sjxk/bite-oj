package com.bite.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ContainerStatus {

    UP("up"),

    CREATED("created"),

    EXITED("exited");


    private String desc;
}
