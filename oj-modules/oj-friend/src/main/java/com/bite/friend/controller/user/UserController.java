package com.bite.friend.controller.user;

import com.bite.common.core.constants.HttpConstants;
import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.vo.LoginUserVO;
import com.bite.friend.domain.user.dto.UserDTO;
import com.bite.friend.service.user.IUserService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Resource(name = "userServiceImpl")
    private IUserService userService;


    @PostMapping("/sendCode")
    public R<Void> sendCode(@Validated @RequestBody UserDTO userDTO) {
        return toR(userService.sendCode(userDTO));
    }

    @PostMapping("/code/login")
    public R<String> codeLogin(@Validated @RequestBody UserDTO userDTO) {
        return R.ok(userService.codeLogin(userDTO.getEmail(), userDTO.getCode()));
    }

    @DeleteMapping("/logout")
    public R<Void> logout(@RequestHeader(HttpConstants.AUTHENTICATION) String token) {
        return userService.logout(token);
    }

    @GetMapping("/info")
    public R<LoginUserVO> info(@RequestHeader(HttpConstants.AUTHENTICATION) String token) {
        return userService.info(token);
    }



}
