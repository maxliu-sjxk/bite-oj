package com.bite.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.domain.R;
import com.bite.common.core.enums.ResultCode;
import com.bite.system.domain.SysUser;
import com.bite.system.mapper.SysUserMapper;
import com.bite.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements ISysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public R<Void> login(String userAccount, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper.select(SysUser::getPassword)
                .eq(SysUser::getUserAccount, userAccount));

        R loginR = new R<>();
        if (sysUser == null) {
            loginR.setCode(ResultCode.FAILED_USER_NOT_EXISTS.getCode());
            loginR.setMsg(ResultCode.FAILED_USER_NOT_EXISTS.getMsg());
        }
        if (!sysUser.getPassword().equals(password)) {
            loginR.setCode(ResultCode.FAILED_LOGIN.getCode());
            loginR.setMsg(ResultCode.FAILED_LOGIN.getMsg());
        }

        loginR.setCode(ResultCode.SUCCESS.getCode());
        loginR.setMsg(ResultCode.SUCCESS.getMsg());
        return loginR;
    }
}
