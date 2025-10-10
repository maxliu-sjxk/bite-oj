package com.bite.system.controller.user;


import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.system.domain.user.dto.UserQueryDTO;
import com.bite.system.domain.user.dto.UserUpdateStatusDTO;
import com.bite.system.service.user.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Resource(name = "userServiceImpl")
    private IUserService userService;


    @GetMapping("/list")
    public TableDataInfo list(UserQueryDTO userQueryDTO) {
        return getTableDataInfo(userService.list(userQueryDTO));
    }

    @PutMapping("/updateStatus")
    public R<Void> updateStatus(@RequestBody UserUpdateStatusDTO userUpdateStatusDTO) {
        return toR(userService.updateStatus(userUpdateStatusDTO));
    }

}
