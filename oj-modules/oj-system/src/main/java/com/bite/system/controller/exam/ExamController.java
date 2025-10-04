package com.bite.system.controller.exam;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.system.domain.exam.dto.ExamAddDTO;
import com.bite.system.domain.exam.dto.ExamEditDTO;
import com.bite.system.domain.exam.dto.ExamQueryDTO;
import com.bite.system.domain.exam.dto.ExamQuestionAddDTO;
import com.bite.system.domain.exam.vo.ExamDetailVO;
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
    public R<String> add(@RequestBody ExamAddDTO examAddDTO) {
        return R.ok(examService.add(examAddDTO));
    }

    @PostMapping("/question/add")
    public R<Void> questionAdd(@RequestBody ExamQuestionAddDTO examQuestionAddDTO) {
        return toR(examService.questionAdd(examQuestionAddDTO));
    }

    @GetMapping("/detail")
    public R<ExamDetailVO> detail(Long examId) {
        return R.ok(examService.detail(examId));
    }

    @PutMapping("/edit")
    public R<Void> edit(@RequestBody ExamEditDTO examEditDTO) {
        return toR(examService.edit(examEditDTO));
    }

    @DeleteMapping("/question/delete")
    public R<Void> questionDelete(Long examId, Long questionId) {
        return toR(examService.questionDelete(examId, questionId));
    }

    @DeleteMapping("/delete")
    public R<Void> delete(Long examId) {
        return toR(examService.delete(examId));
    }
}
