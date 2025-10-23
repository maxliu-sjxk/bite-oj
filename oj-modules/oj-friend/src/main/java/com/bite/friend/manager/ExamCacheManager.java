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
import com.bite.friend.mapper.user.UserExamMapper;
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

    @Autowired
    private UserExamMapper userExamMapper;


    public Long getExamListSize(Integer examListType, Long userId) {
        String examListKey = getExamListKey(examListType, userId);
        return redisService.getListSize(examListKey);
    }

    private String getExamListKey(Integer examListType, Long userId) {
        if (ExamListType.EXAM_UNFINISHED_LIST.getValue().equals(examListType)) {
            return CacheConstants.EXAM_UNFINISHED_LIST_KEY;
        } else if (ExamListType.EXAM_HISTORY_LIST.getValue().equals(examListType)){
            return CacheConstants.EXAM_HISTORY_LIST_KEY;
        } else {
            return getUserExamListKey(userId);
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
    public void refreshCache(List<ExamVO> examVOList, Integer examListType, Long userId) {
        if (CollectionUtil.isEmpty(examVOList)) {
            return;
        }
        //对象转换
        List<Exam> examList = examVoToExamConverter.voListToEntityList(examVOList);
        //通过Redis批量插入/设置竞赛详情缓存
        Map<String, Exam> examMap = new HashMap<>(); //用于竞赛详情缓存的批量写入
        List<Long> examIdList = new ArrayList<>(); //用于竞赛列表写入
        for (Exam exam : examList) {
            examMap.put(getExamDetailKey(exam.getExamId()), exam);
            examIdList.add(exam.getExamId());
        }
        redisService.multiSet(examMap);
        //如果redis中数据有误，直接接着push会导致数据重复或继续有误，因此先删除
        redisService.deleteObject(getExamListKey(examListType, userId));
        redisService.rightPushAll(getExamListKey(examListType, userId), examIdList);
    }


    /**
     * 从缓存中获取竞赛列表
     * TODO 如果用户选择了startTime或endTime过滤，则需要额外处理
     * 方案：
     * 将缓存中列表的所有数据（id）查询出来，然后根据id从缓存中查出所有的竞赛详情信息，拿到后需要根据过滤条件进行数据的筛选，
     * 筛选结束后得到一个新的列表，新列表就是符合过滤条件的所有竞赛：
     * total => 该列表的记录总数
     * rows => 列表数据分页后得到的新列表（具体分页逻辑需要自行处理）
     *
     * 上述思路涉及到的所有方法都需要修改
     * @param examQueryDTO
     * @return
     */
    public List<ExamVO> getExamVOListFromCache(ExamQueryDTO examQueryDTO, Long userId) {
        int start = (examQueryDTO.getPageNum() - 1) * examQueryDTO.getPageSize();
        int end = start + examQueryDTO.getPageSize() - 1;
        String examListKey = getExamListKey(examQueryDTO.getType(), userId);
        List<Long> examIdList = redisService.getCacheListByRange(examListKey, start, end, Long.class);
        List<ExamVO> examVOList = assembleExamVOList(examIdList);
        if (CollectionUtil.isEmpty(examVOList)) {
            //Redis数据有问题，查询数据库
            examVOList = getExamVOListFromDB(examQueryDTO, userId);
            refreshCache(examVOList, examQueryDTO.getType(), userId);
        }
        return examVOList;
    }

    private List<ExamVO> getExamVOListFromDB(ExamQueryDTO examQueryDTO, Long userId) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        if (examQueryDTO.getType().equals(ExamListType.USER_EXAM_LIST.getValue())) {
            //"我的竞赛"分支
            return userExamMapper.selectUserExamList(userId);
        } else {
            //竞赛列表分支
            return examMapper.selectExamList(examQueryDTO);
        }
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

    public void cacheUserExamList(Long userId, Long examId) {
        String userExamListKey = getUserExamListKey(userId);
        redisService.leftPushForList(userExamListKey, examId);
    }

    private String getUserExamListKey(Long userId) {
        return CacheConstants.USER_EXAM_LIST_KEY_PREFIX + userId;
    }
}
