package com.bite.friend.service.question;


import com.bite.common.core.domain.TableDataInfo;
import com.bite.friend.domain.question.dto.QuestionQueryDTO;
import com.bite.friend.domain.question.vo.QuestionDetailVO;
import com.bite.friend.domain.question.vo.QuestionVO;

import java.util.List;

public interface IQuestionService {
    TableDataInfo list(QuestionQueryDTO questionQueryDTO);

    QuestionDetailVO detail(Long questionId);

    String preQuestion(Long questionId);

    String nextQuestion(Long questionId);

    List<QuestionVO> hotList();
}
