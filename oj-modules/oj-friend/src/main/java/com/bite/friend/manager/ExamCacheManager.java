package com.bite.friend.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.enums.ExamListType;
import com.bite.common.redis.service.RedisService;
import com.bite.friend.domain.exam.Exam;
import com.bite.friend.domain.exam.dto.ExamQueryDTO;
import com.bite.friend.domain.exam.vo.ExamVO;
import com.bite.friend.mapper.exam.ExamMapper;
import com.bite.friend.mapstruct.ExamVoToExamMapper;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExamCacheManager {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ExamVoToExamMapper examVoToExamConverter;

    @Autowired
    private ExamMapper examMapper;


    public Long getExamListSize(Integer examListType) {
        String examListKey = getExamListKey(examListType);
        return redisService.getListSize(examListKey);
    }

    private String getExamListKey(Integer examListType) {
        if (ExamListType.EXAM_UNFINISHED_LIST.getValue().equals(examListType)) {
            return CacheConstants.EXAM_UNFINISHED_LIST_KEY;
        } else {
            return CacheConstants.EXAM_HISTORY_LIST_KEY;
        }
    }

    public String getExamDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL_KEY_PREFIX + examId;
    }

    /**
     * 前提：从数据库中查询数据后
     * 刷新缓存：刷新list缓存；刷新竞赛string详情缓存
     *
     * 遗留问题：
     * 是否需要在此再查询数据库
     * 有必要将examVOList转换为examList吗
     *
     * 目前实现：将service的数据库查询结果直接缓存
     */
    public void refreshCache(List<ExamVO> examVOList, Integer examListType) {
        if (CollectionUtil.isEmpty(examVOList)) {
            return;
        }
        //对象转换
        List<Exam> examList = examVoToExamConverter.voListToEntityList(examVOList);
        //通过Redis批量插入/设置竞赛详情缓存
        Map<String, Exam> examMap = new HashMap<>();
        List<Long> examIdList = new ArrayList<>();
        for (Exam exam : examList) {
            examMap.put(getExamDetailKey(exam.getExamId()), exam);
            examIdList.add(exam.getExamId());
        }
        redisService.multiSet(examMap);
        //如果redis中数据有误，直接接着push会导致数据重复或继续有误，因此先删除
        redisService.deleteObject(getExamListKey(examListType));
        redisService.rightPushAll(getExamListKey(examListType), examIdList);
    }

    public List<ExamVO> getExamVOListFromCache(ExamQueryDTO examQueryDTO) {
        int start = (examQueryDTO.getPageNum() - 1) * examQueryDTO.getPageSize();
        int end = start + examQueryDTO.getPageSize() - 1;
        String examListKey = getExamListKey(examQueryDTO.getType());
        List<Long> examIdList = redisService.getCacheListByRange(examListKey, start, end, Long.class);
        List<ExamVO> examVOList = assembleExamVOList(examIdList);
        if (CollectionUtil.isEmpty(examVOList)) {
            //Redis数据有问题，查询数据库
            examVOList = getExamVOListFromDB(examQueryDTO);
            refreshCache(examVOList, examQueryDTO.getType());
        }
        return examVOList;
    }

    private List<ExamVO> getExamVOListFromDB(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    private List<ExamVO> assembleExamVOList(List<Long> examIdList) {
        if (CollectionUtil.isEmpty(examIdList)) {
            return null;
        }
        //构造ExamDetailKeyList
        List<String> examDetailKeyList = examIdList.stream().map(examId -> getExamDetailKey(examId)).toList();
        List<ExamVO> examVOList = redisService.multiGet(examDetailKeyList, ExamVO.class);
        CollUtil.removeNull(examVOList);
        if (CollectionUtil.isEmpty(examVOList) || examVOList.size() != examIdList.size()) {
            return null;
        }
        return examVOList;
    }
}
