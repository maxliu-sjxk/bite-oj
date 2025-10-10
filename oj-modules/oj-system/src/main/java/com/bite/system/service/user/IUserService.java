package com.bite.system.service.user;

import com.bite.system.domain.user.dto.UserQueryDTO;
import com.bite.system.domain.user.dto.UserUpdateStatusDTO;
import com.bite.system.domain.user.vo.UserVO;

import java.util.List;

public interface IUserService {
    List<UserVO> list(UserQueryDTO userQueryDTO);

    int updateStatus(UserUpdateStatusDTO userUpdateStatusDTO);
}
