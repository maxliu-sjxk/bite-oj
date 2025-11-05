package com.bite.system.manager;

import com.bite.common.core.constants.CacheConstants;
import com.bite.common.redis.service.RedisService;
import com.bite.system.domain.exam.Exam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExamCacheManager {

    @Autowired
    private RedisService redisService;

    /**
     * 缓存新发布的竞赛
     * @param exam
     */
    public void addCache(Exam exam) {
        redisService.leftPushForList(getUnfinishedExamListKey(), exam.getExamId());
        redisService.setCacheObject(getExamDetailKey(exam.getExamId()), exam);
    }

    /**
     * 从缓存中移除被撤销发布的竞赛
     * 从缓存中移除被撤销发布的竞赛的题目列表
     * @param examId
     */
    public void deleteCache(Long examId) {
        redisService.removeForList(getUnfinishedExamListKey(), examId);
        redisService.deleteObject(getExamDetailKey(examId));
        redisService.deleteObject(getExamQuestionListKey(examId));
    }

    private String getUnfinishedExamListKey() {
        return CacheConstants.EXAM_UNFINISHED_LIST_KEY;
    }

    private String getExamDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL_KEY_PREFIX + examId;
    }

    private String getExamQuestionListKey(Long examId) {
        return CacheConstants.EXAM_QUESTION_LIST_KEY_PREFIX + examId;
    }
}
