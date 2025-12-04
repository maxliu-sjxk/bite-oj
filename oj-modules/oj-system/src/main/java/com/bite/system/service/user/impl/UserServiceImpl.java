package com.bite.system.service.user.impl;

import com.bite.common.core.enums.ResultCode;
import com.bite.common.security.exception.ServiceException;
import com.bite.system.domain.user.User;
import com.bite.system.domain.user.dto.UserQueryDTO;
import com.bite.system.domain.user.dto.UserUpdateStatusDTO;
import com.bite.system.domain.user.vo.UserVO;
import com.bite.system.manager.UserCacheManager;
import com.bite.system.mapper.user.UserMapper;
import com.bite.system.service.user.IUserService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserCacheManager userCacheManager;

    @Override
    public List<UserVO> list(UserQueryDTO userQueryDTO) {
        PageHelper.startPage(userQueryDTO.getPageNum(), userQueryDTO.getPageSize());
        return userMapper.selectUserList(userQueryDTO);
    }

    @Override
    public int updateStatus(UserUpdateStatusDTO userUpdateStatusDTO) {
        User user = userMapper.selectById(userUpdateStatusDTO.getUserId());
        if (user == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        user.setStatus(userUpdateStatusDTO.getStatus());
        userCacheManager.updateUserStatusCache(user.getUserId(), user.getStatus());
        return userMapper.updateById(user);
    }
}
