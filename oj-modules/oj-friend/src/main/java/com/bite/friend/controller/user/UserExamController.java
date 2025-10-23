package com.bite.friend.controller.user;

import com.bite.common.core.constants.HttpConstants;
import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.friend.domain.exam.dto.ExamDTO;
import com.bite.friend.domain.exam.dto.ExamQueryDTO;
import com.bite.friend.service.user.IUserExamService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/exam")
public class UserExamController extends BaseController {

    @Resource(name = "userExamServiceImpl")
    private IUserExamService userExamService;


    @PostMapping("/enter")
    public R<Void> enter(@RequestBody ExamDTO examDTO) {
        return toR(userExamService.enter(examDTO.getExamId()));
    }


    @GetMapping("/list")
    public TableDataInfo list(ExamQueryDTO examQueryDTO) {
        return userExamService.list(examQueryDTO);
    }

}
