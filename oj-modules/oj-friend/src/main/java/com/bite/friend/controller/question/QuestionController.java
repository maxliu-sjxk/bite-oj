package com.bite.friend.controller.question;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.friend.domain.question.dto.QuestionQueryDTO;
import com.bite.friend.domain.question.vo.QuestionDetailVO;
import com.bite.friend.service.question.IQuestionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/question")
public class QuestionController extends BaseController {

    @Resource(name = "questionServiceImpl")
    private IQuestionService questionService;


    @GetMapping("/semiLogin/list")
    public TableDataInfo list(QuestionQueryDTO questionQueryDTO) {
        return questionService.list(questionQueryDTO);
    }

    @GetMapping("/detail")
    public R<QuestionDetailVO> detail(Long questionId) {
        return R.ok(questionService.detail(questionId));
    }

    @GetMapping("/preQuestion")
    public R<String> preQuestion(Long questionId) {
        return R.ok(questionService.preQuestion(questionId));
    }

    @GetMapping("/nextQuestion")
    public R<String> nextQuestion(Long questionId) {
        return R.ok(questionService.nextQuestion(questionId));
    }

}
