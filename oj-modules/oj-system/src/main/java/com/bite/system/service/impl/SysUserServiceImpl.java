package com.bite.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.domain.R;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.core.enums.UserIdentity;
import com.bite.common.security.service.TokenService;
import com.bite.system.domain.SysUser;
import com.bite.system.mapper.SysUserMapper;
import com.bite.system.service.ISysUserService;
import com.bite.system.utils.BCryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@RefreshScope
@Service
public class SysUserServiceImpl implements ISysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Value("${jwt.secret}")
    private String secret;//需要定期更换，避免硬编码，交由Nacos统一配置管理

    @Autowired
    private TokenService tokenService;

    @Override
    public R<String> login(String userAccount, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper.select(SysUser::getUserId, SysUser::getPassword)
                .eq(SysUser::getUserAccount, userAccount));
        if (sysUser == null) {
            return R.fail(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if (!BCryptUtils.matchesPassword(password, sysUser.getPassword())) {
            return R.fail(ResultCode.FAILED_LOGIN);
        }
        //登陆成功
        String token = tokenService.createTokenAndCache(sysUser.getUserId(), secret, UserIdentity.ADMIN);
        return R.ok(token);
    }
}
