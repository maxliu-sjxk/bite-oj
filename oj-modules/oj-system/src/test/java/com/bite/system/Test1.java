package com.bite.system;

import com.bite.system.domain.question.dto.QuestionQueryDTO;
import com.bite.system.mapper.question.QuestionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test1 {


    @Autowired
    private QuestionMapper questionMapper;

    @Test
    public void test1() {
        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        questionQueryDTO.setPageNum(1);
        questionQueryDTO.setPageSize(10);
        System.out.println(questionMapper.selectQuestionList(questionQueryDTO));
    }
}
