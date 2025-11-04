package com.bite.system.manager;

import com.bite.common.core.constants.CacheConstants;
import com.bite.common.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QuestionCacheManager {

    @Autowired
    private RedisService redisService;


    public void addCache(long questionId) {
        redisService.leftPushForList(CacheConstants.QUESTION_LIST_KEY, questionId);
    }


    public void deleteCache(long questionId) {
        redisService.removeForList(CacheConstants.QUESTION_LIST_KEY, questionId);
    }

}
