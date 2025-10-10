package com.bite.system.service.user.impl;

import com.bite.system.domain.user.dto.UserQueryDTO;
import com.bite.system.domain.user.vo.UserVO;
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

    @Override
    public List<UserVO> list(UserQueryDTO userQueryDTO) {
        PageHelper.startPage(userQueryDTO.getPageNum(), userQueryDTO.getPageSize());
        return userMapper.selectUserList(userQueryDTO);
    }
}
