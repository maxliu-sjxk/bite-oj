package com.bite.system.domain.question.vo;

import com.bite.system.domain.question.dto.QuestionAddDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionDetailVO {

    private Long questionId;

    private String title;

    private Integer difficulty;

    private Long timeLimit;

    private Long spaceLimit;

    private String content;

    private String questionCase;

    private String defaultCode;

    private String mainFunc;
}
