package com.bite.system.controller.question;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.system.domain.question.dto.QuestionQueryDTO;
import com.bite.system.service.question.IQuestionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/question")
public class QuestionController extends BaseController {


    @Resource(name = "questionServiceImpl")
    private IQuestionService questionService;

    @GetMapping("/list")
    public TableDataInfo list(QuestionQueryDTO questionQueryDTO) {
        return getTableDataInfo(questionService.list(questionQueryDTO));
    }
}
