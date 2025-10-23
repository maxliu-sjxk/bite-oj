package com.bite.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExamListType {

    EXAM_UNFINISHED_LIST(0),

    EXAM_HISTORY_LIST(1),

    USER_EXAM_LIST(2);

    private final Integer value;

}
