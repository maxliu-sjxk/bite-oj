package com.bite.friend.manager;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.redis.service.RedisService;
import com.bite.friend.domain.user.User;
import com.bite.friend.domain.user.vo.UserVO;
import com.bite.friend.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserCacheManager {

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

    public UserVO getUserCacheById(Long userId) {
        String userDetailKey = getUserDetailKey(userId);
        UserVO userVO = redisService.getCacheObject(userDetailKey, UserVO.class);
        if (userVO != null) {
            redisService.expire(userDetailKey, CacheConstants.USER_EXP, TimeUnit.MINUTES);
            return userVO;
        }
        //缓存未命中，查询数据库
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getUserId,
                        User::getNickName,
                        User::getHeadImage,
                        User::getSex,
                        User::getEmail,
                        User::getPhone,
                        User::getWechat,
                        User::getIntroduce,
                        User::getSchoolName,
                        User::getMajorName,
                        User::getStatus)
                .eq(User::getUserId, userId));
        if (user == null) {
            return null;
        }
        refreshUserCache(user);
        userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    public void refreshUserCache(User user) {
        String userDetailKey = getUserDetailKey(user.getUserId());
        redisService.setCacheObject(userDetailKey, user, CacheConstants.USER_EXP, TimeUnit.MINUTES);
    }

    private String getUserDetailKey(Long userId) {
        return CacheConstants.USER_DETAIL_KEY_PREFIX + userId;
    }
}
