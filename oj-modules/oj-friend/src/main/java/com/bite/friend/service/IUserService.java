package com.bite.friend.service;

import com.bite.common.core.domain.R;
import com.bite.common.core.domain.vo.LoginUserVO;
import com.bite.friend.domain.dto.UserDTO;

public interface IUserService {

    boolean sendCode(UserDTO userDTO);

    String codeLogin(String email, String code);

    R<Void> logout(String token);

    R<LoginUserVO> info(String token);
}
