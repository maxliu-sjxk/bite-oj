package com.bite.system.service;

import com.bite.common.core.domain.R;
import com.bite.system.domain.dto.SysUserSaveDTO;
import com.bite.system.domain.vo.LoginUserVO;

public interface ISysUserService {
    R<String> login(String userAccount, String password);

    int add(SysUserSaveDTO sysUserSaveDTO);

    R<LoginUserVO> info(String token);
}
