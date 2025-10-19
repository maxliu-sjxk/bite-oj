package com.bite.job.handler;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.Constants;
import com.bite.common.redis.service.RedisService;
import com.bite.job.domain.Exam;
import com.bite.job.mapper.ExamMapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ExamXxlJob {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private RedisService redisService;

    @XxlJob("examListOrganizeHandler")
    public void examListOrganizeHandler() {
        log.info("*** examListOrganizeHandler 开始 ***");
        List<Exam> unFinishedExamList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                .gt(Exam::getEndTime, LocalDateTime.now())
                .eq(Exam::getStatus, Constants.TRUE)
                .orderByDesc(Exam::getCreateTime));
        refreshCache(unFinishedExamList, CacheConstants.EXAM_UNFINISHED_LIST_KEY);

        List<Exam> historyExamList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                .le(Exam::getEndTime, LocalDateTime.now())
                .eq(Exam::getStatus, Constants.TRUE)
                .orderByDesc(Exam::getCreateTime));
        refreshCache(historyExamList, CacheConstants.EXAM_HISTORY_LIST_KEY);
        log.info("*** examListOrganizeHandler 结束 ***");
    }


    private void refreshCache(List<Exam> examList, String examListKey) {
        if (CollectionUtil.isEmpty(examList)) {
            return;
        }

        //通过Redis批量插入/设置竞赛详情缓存
        Map<String, Exam> examMap = new HashMap<>();
        List<Long> examIdList = new ArrayList<>();
        for (Exam exam : examList) {
            examMap.put(getExamDetailKey(exam.getExamId()), exam);
            examIdList.add(exam.getExamId());
        }
        redisService.multiSet(examMap);
        //如果redis中数据有误，直接接着push会导致数据重复或继续有误，因此先删除
        redisService.deleteObject(examListKey);
        redisService.rightPushAll(examListKey, examIdList);
    }

    private String getExamDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL_KEY_PREFIX + examId;
    }


}
