package com.bite.system.service.sysuser;

import com.bite.common.core.domain.R;
import com.bite.system.domain.sysuser.dto.SysUserSaveDTO;
import com.bite.common.core.domain.vo.LoginUserVO;

public interface ISysUserService {
    R<String> login(String userAccount, String password);

    R<Void> logout(String token);

    R<LoginUserVO> info(String token);

    int add(SysUserSaveDTO sysUserSaveDTO);

}
