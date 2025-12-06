package com.bite.friend.manager;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.redis.service.RedisService;
import com.bite.common.security.exception.ServiceException;
import com.bite.friend.domain.question.Question;
import com.bite.friend.mapper.question.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionCacheManager {

    @Autowired
    private RedisService redisService;


    @Autowired
    private QuestionMapper questionMapper;


    public Long getQuestionListSize() {
        return redisService.getListSize(CacheConstants.QUESTION_LIST_KEY);
    }

    public void refreshCache() {
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .select(Question::getQuestionId)
                .orderByDesc(Question::getCreateTime));
        if (CollectionUtil.isEmpty(questionList)) {
            return;
        }
        List<Long> questionIdList = questionList.stream().map(Question::getQuestionId).toList();
        redisService.rightPushAll(CacheConstants.QUESTION_LIST_KEY, questionIdList);
    }

    public void refreshHotQuestionList(List<Long> hotQuestionIdList) {
        if (CollectionUtil.isEmpty(hotQuestionIdList)) {
            return;
        }
        redisService.rightPushAll(CacheConstants.HOT_QUESTION_LIST_KEY, hotQuestionIdList);
    }

    public Long preQuestion(Long questionId) {
        long index = redisService.indexOfForList(CacheConstants.QUESTION_LIST_KEY, questionId);
        if (index == 0) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_FIRST_QUESTION);
        }
        return redisService.indexForList(CacheConstants.QUESTION_LIST_KEY, index - 1, Long.class);
    }

    public Long nextQuestion(Long questionId) {
        long index = redisService.indexOfForList(CacheConstants.QUESTION_LIST_KEY, questionId);
        long lastIndex = getQuestionListSize() - 1;
        if (index == lastIndex) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_LAST_QUESTION);
        }
        return redisService.indexForList(CacheConstants.QUESTION_LIST_KEY, index + 1, Long.class);
    }

    public Long getHotQuestionListSize() {
        return redisService.getListSize(CacheConstants.HOT_QUESTION_LIST_KEY);
    }

    public List<Long> getHostQuestionListFromCache() {
        return redisService.getCacheListByRange(CacheConstants.HOT_QUESTION_LIST_KEY,
                CacheConstants.DEFAULT_START, CacheConstants.DEFAULT_END, Long.class);
    }

}
