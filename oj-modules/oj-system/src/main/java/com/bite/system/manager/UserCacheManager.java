package com.bite.system.manager;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.redis.service.RedisService;
import com.bite.system.domain.user.User;
import com.bite.system.domain.user.vo.UserVO;
import com.bite.system.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserCacheManager {

    @Autowired
    private RedisService redisService;

    public void updateUserStatusCache(Long userId, Integer status) {
        String userDetailKey = getUserDetailKey(userId);
        User user = redisService.getCacheObject(userDetailKey, User.class);
        if (user == null) {
            return;
        }
        user.setStatus(status);
        redisService.setCacheObject(userDetailKey, user, CacheConstants.USER_EXP, TimeUnit.MINUTES);
    }

    private String getUserDetailKey(Long userId) {
        return CacheConstants.USER_DETAIL_KEY_PREFIX + userId;
    }
}
