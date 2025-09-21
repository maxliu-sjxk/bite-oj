package com.bite.system.controller.exam;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.system.domain.exam.dto.ExamAddDTO;
import com.bite.system.domain.exam.dto.ExamQueryDTO;
import com.bite.system.domain.exam.dto.ExamQuestionAddDTO;
import com.bite.system.service.exam.IExamService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exam")
public class ExamController extends BaseController {

    @Resource(name = "examServiceImpl")
    private IExamService examService;

    @GetMapping("/list")
    public TableDataInfo list(ExamQueryDTO examQueryDTO) {
        return getTableDataInfo(examService.list(examQueryDTO));
    }

    @PostMapping("/add")
    public R<Void> add(@RequestBody ExamAddDTO examAddDTO) {
        return toR(examService.add(examAddDTO));
    }

    @PostMapping("/question/add")
    public R<Void> questionAdd(@RequestBody ExamQuestionAddDTO examQuestionAddDTO) {
        return toR(examService.questionAdd(examQuestionAddDTO));
    }
}
