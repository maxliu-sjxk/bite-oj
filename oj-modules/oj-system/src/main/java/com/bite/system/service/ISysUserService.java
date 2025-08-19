package com.bite.system.service;

import com.bite.common.core.domain.R;
import com.bite.system.domain.LoginDTO;

public interface ISysUserService {
    R<String> login(String userAccount, String password);
}
