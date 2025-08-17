package com.bite.system.service;

import com.bite.common.core.domain.R;
import com.bite.system.domain.LoginDTO;

public interface ISysUserService {
    R<Void> login(String userAccount, String password);
}
