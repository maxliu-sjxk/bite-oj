package com.bite.job.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.Constants;
import com.bite.common.redis.service.RedisService;
import com.bite.job.mapper.user.UserSubmitMapper;
import com.github.pagehelper.PageHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class QuestionXxlJob {

    @Autowired
    private UserSubmitMapper userSubmitMapper;

    @Autowired
    private RedisService redisService;

    @XxlJob("hotQuestionListHandler")
    public void hotQuestionListHandler() {
        log.info("----- 题目热门列表统计开始 ------");
        PageHelper.startPage(Constants.HOT_QUESTION_LIST_START, Constants.HOT_QUESTION_LIST_END);
        List<Long> questionIdList = userSubmitMapper.selectHotQuestionList();
        if (CollectionUtil.isEmpty(questionIdList)) {
            return;
        }
        redisService.deleteObject(CacheConstants.HOT_QUESTION_LIST_KEY);
        redisService.rightPushAll(CacheConstants.HOT_QUESTION_LIST_KEY, questionIdList);
        log.info("----- 题目热门列表统计结束 ------");
    }
}
