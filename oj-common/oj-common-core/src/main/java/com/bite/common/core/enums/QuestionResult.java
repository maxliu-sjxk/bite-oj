package com.bite.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum QuestionResult {

    ERROR(0),
    PASS(1);

    private Integer value;
}
