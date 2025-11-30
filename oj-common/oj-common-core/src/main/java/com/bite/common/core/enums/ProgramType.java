package com.bite.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProgramType {

    JAVA(0, "Java语言"),
    CPP(1, "C++语言"),
    PYTHON(2, "Python语言");

    private Integer value;

    private String desc;
}
