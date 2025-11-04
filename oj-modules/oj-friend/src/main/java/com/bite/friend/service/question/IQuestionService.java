package com.bite.friend.service.question;


import com.bite.common.core.domain.TableDataInfo;
import com.bite.friend.domain.question.dto.QuestionQueryDTO;
import com.bite.friend.domain.question.vo.QuestionDetailVO;

public interface IQuestionService {
    TableDataInfo list(QuestionQueryDTO questionQueryDTO);

    QuestionDetailVO detail(Long questionId);

    String preQuestion(Long questionId);

    String nextQuestion(Long questionId);
}
