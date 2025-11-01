package com.bite.friend.service.user;

import com.bite.common.core.domain.R;
import com.bite.common.core.domain.vo.LoginUserVO;
import com.bite.friend.domain.user.dto.UserDTO;
import com.bite.friend.domain.user.vo.UserVO;

public interface IUserService {

    boolean sendCode(UserDTO userDTO);

    String codeLogin(String email, String code);

    R<Void> logout(String token);

    R<LoginUserVO> info(String token);

    R<UserVO> detail();
}
