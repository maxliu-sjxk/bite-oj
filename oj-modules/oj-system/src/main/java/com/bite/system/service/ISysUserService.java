package com.bite.system.service;

import com.bite.common.core.domain.R;
import com.bite.system.domain.dto.SysUserSaveDTO;

public interface ISysUserService {
    R<String> login(String userAccount, String password);

    int add(SysUserSaveDTO sysUserSaveDTO);
}
