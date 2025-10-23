package com.bite.friend.service.user.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.common.core.enums.ExamListType;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.core.utils.ThreadLocalUtils;
import com.bite.common.security.exception.ServiceException;
import com.bite.friend.domain.exam.Exam;
import com.bite.friend.domain.exam.dto.ExamQueryDTO;
import com.bite.friend.domain.exam.vo.ExamVO;
import com.bite.friend.domain.user.UserExam;
import com.bite.friend.manager.ExamCacheManager;
import com.bite.friend.mapper.exam.ExamMapper;
import com.bite.friend.mapper.user.UserExamMapper;
import com.bite.friend.service.user.IUserExamService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserExamServiceImpl implements IUserExamService {

    @Autowired
    private UserExamMapper userExamMapper;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamCacheManager examCacheManager;


    @Override
    public int enter(Long examId) {
        Exam exam = examMapper.selectById(examId);
        //判断竞赛是否存在
        if (exam == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        //判断竞赛是否已开赛
        if (exam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_ALREADY_STARTED);
        }
        Long userId = ThreadLocalUtils.get(Constants.USER_ID, Long.class);
        //判断是否重复报名
        UserExam userExam = userExamMapper.selectOne(new LambdaQueryWrapper<UserExam>()
                .eq(UserExam::getExamId, examId)
                .eq(UserExam::getUserId, userId));
        if (userExam != null) {
            throw new ServiceException(ResultCode.FAILED_REPEATED_ENTER);
        }
        //写入缓存
        examCacheManager.cacheUserExamList(userId, examId);
        //写入数据库
        userExam = new UserExam();
        userExam.setExamId(examId);
        userExam.setUserId(userId);
        return userExamMapper.insert(userExam);
    }

    @Override
    public TableDataInfo list(ExamQueryDTO examQueryDTO) {
        //首先设置一下dto的type（Integer）
        examQueryDTO.setType(ExamListType.USER_EXAM_LIST.getValue());
        Long userId = ThreadLocalUtils.get(Constants.USER_ID, Long.class);
        Long total = examCacheManager.getExamListSize(examQueryDTO.getType(), userId);
        List<ExamVO> examVOList;
        if (total == null || total <= 0L) {
            //缓存未命中，直接查询数据库
            PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
            examVOList = userExamMapper.selectUserExamList(userId);
            //刷新缓存
            examCacheManager.refreshCache(examVOList, examQueryDTO.getType(), userId);
            total = new PageInfo<>(examVOList).getTotal();
        } else {
            //缓存命中，直接查询缓存
            examVOList = examCacheManager.getExamVOListFromCache(examQueryDTO, userId);
            total = examCacheManager.getExamListSize(examQueryDTO.getType(), userId);
        }
        if (CollectionUtil.isEmpty(examVOList)) {
            return TableDataInfo.empty();
        }
        return TableDataInfo.success(examVOList, total);
    }
}
