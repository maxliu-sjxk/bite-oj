package com.bite.friend.controller;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.friend.domain.dto.UserDTO;
import com.bite.friend.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Resource(name = "userServiceImpl")
    private IUserService userService;


    @PostMapping("/sendCode")
    public R<Void> sendCode(@Validated @RequestBody UserDTO userDTO) {
        return toR(userService.sendCode(userDTO));
    }

}
