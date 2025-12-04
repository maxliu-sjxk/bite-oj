package com.bite.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum QuestionResStatus {

    NOT_PASSED(0, "未通过"),

    PASSED(1, "通过"),

    NOT_SUBMIT(2, "未提交"),

    JUDGING(3, "判题中");


    private Integer value;
    private String desc;
}
