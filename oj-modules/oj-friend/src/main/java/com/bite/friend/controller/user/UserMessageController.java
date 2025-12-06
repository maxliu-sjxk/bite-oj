package com.bite.friend.controller.user;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.PageQueryDTO;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.friend.service.user.IUserMessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/message")
public class UserMessageController extends BaseController {

    @Resource(name = "userMessageServiceImpl")
    private IUserMessageService userMessageService;

    @GetMapping("/list")
    public TableDataInfo list(PageQueryDTO pageQueryDTO) {
        return userMessageService.list(pageQueryDTO);
    }

}
