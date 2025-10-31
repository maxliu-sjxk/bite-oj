package com.bite.friend.mapper.question;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bite.friend.domain.question.Question;
import com.bite.friend.domain.question.dto.QuestionQueryDTO;
import com.bite.friend.domain.question.vo.QuestionVO;

import java.util.List;

public interface QuestionMapper extends BaseMapper<Question> {
    List<QuestionVO> selectQuestionList(QuestionQueryDTO questionQueryDTO);
}
