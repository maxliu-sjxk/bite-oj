package com.bite.friend.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSubmitDTO {

    private Long questionId;

    /**
     * 竞赛Id（选传）
     */
    private Long examId;

    private String userCode;

    private Integer programType; //0->Java 1->CPP 2->Python
}
