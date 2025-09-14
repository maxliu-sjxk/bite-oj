package com.bite.system.controller;

import com.bite.common.core.constants.HttpConstants;
import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.system.domain.dto.LoginDTO;
import com.bite.system.domain.dto.SysUserSaveDTO;
import com.bite.system.domain.vo.LoginUserVO;
import com.bite.system.domain.vo.SysUserVO;
import com.bite.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员用户API")
@RestController
@RequestMapping("/sysUser")
public class SysUserController extends BaseController {

    @Resource(name = "sysUserServiceImpl")
    private ISysUserService sysUserService;

    @Operation(summary = "管理员登录", description = "根据用户和密码信息登录")
    @ApiResponse(responseCode = "1000", description = "操作成功")
    @ApiResponse(responseCode = "2000", description = "服务器繁忙请稍后重试")
    @ApiResponse(responseCode = "3102", description = "用户不存在")
    @ApiResponse(responseCode = "3103", description = "用户名或密码错误")
    @PostMapping("/login")
    public R<String> login(@RequestBody LoginDTO loginDTO) {
        return sysUserService.login(loginDTO.getUserAccount(), loginDTO.getPassword());
    }

    @Operation(summary = "新增管理员", description = "根据提供的信息新增管理员")
    @ApiResponse(responseCode = "1000", description = "操作成功")
    @ApiResponse(responseCode = "2000", description = "服务器繁忙请稍后重试")
    @ApiResponse(responseCode = "3101", description = "用户已存在")
    @PostMapping("/add")
    public R<Void> add(@RequestBody SysUserSaveDTO sysUserSaveDTO) {
        return toR(sysUserService.add(sysUserSaveDTO));
    }

    @GetMapping("/info")
    public R<LoginUserVO> info(@RequestHeader(HttpConstants.AUTHENTICATION) String token) {
        return sysUserService.info(token);
    }

    @Operation(summary = "删除管理员", description = "根据用户ID删除管理员")
    @Parameters(value = { @Parameter(name = "userId", in = ParameterIn.PATH, description = "用户ID") })
    @ApiResponse(responseCode = "1000", description = "成功删除管理员信息")
    @ApiResponse(responseCode = "2000", description = "服务器繁忙请稍后重试")
    @ApiResponse(responseCode = "3102", description = "用户不存在")
    @DeleteMapping("/{userId}")
    public R<Void> delete(@PathVariable Long userId) {
        return null;
    }

    @Operation(summary = "管理员详情", description = "根据用户ID获取管理员详情信息")
    @Parameters(value = {
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "用户ID"),
            @Parameter(name = "sex", in = ParameterIn.QUERY, description = "管理员性别")
    })
    @ApiResponse(responseCode = "1000", description = "成功获取管理员详情信息")
    @ApiResponse(responseCode = "2000", description = "服务器繁忙请稍后重试")
    @ApiResponse(responseCode = "3102", description = "用户不存在")
    @GetMapping("/detail")
    public R<SysUserVO> detail(@RequestParam(required = true) Long userId, @RequestParam(required = false) String sex) {
        return null;
    }

}
