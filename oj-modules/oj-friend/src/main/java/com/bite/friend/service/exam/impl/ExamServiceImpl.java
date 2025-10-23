package com.bite.friend.service.exam.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.common.core.utils.ThreadLocalUtils;
import com.bite.friend.domain.exam.dto.ExamQueryDTO;
import com.bite.friend.domain.exam.vo.ExamVO;
import com.bite.friend.manager.ExamCacheManager;
import com.bite.friend.mapper.exam.ExamMapper;
import com.bite.friend.service.exam.IExamService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamServiceImpl implements IExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamCacheManager examCacheManager;


    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    /**
     * 获取竞赛列表
     * 流程：
     * 先查询缓存，缓存没有则查询数据库
     * @param examQueryDTO
     * @return
     */
    @Override
    public TableDataInfo redisList(ExamQueryDTO examQueryDTO) {
        //查询缓存中竞赛list的大小
        Long total = examCacheManager.getExamListSize(examQueryDTO.getType(), null);
        List<ExamVO> examVOList;
        if (total == null || total <= 0L) {
            //缓存未命中，直接查询数据库
            PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
            examVOList = examMapper.selectExamList(examQueryDTO);
            //刷新缓存
            examCacheManager.refreshCache(examVOList, examQueryDTO.getType(), null);
            total = new PageInfo<>(examVOList).getTotal();
        } else {
            //缓存命中，直接查询缓存
            examVOList = examCacheManager.getExamVOListFromCache(examQueryDTO, null);
            total = examCacheManager.getExamListSize(examQueryDTO.getType(), null);
        }
        if (CollectionUtil.isEmpty(examVOList)) {
            return TableDataInfo.empty();
        }
        //返回报名状态（即填充ExamVO的enter字段）
        //需要查出用户报名的所有竞赛，然后遍历examVOList，判断其examId是否在用户报名的竞赛列表中
        assembleExamVOList(examVOList);
        return TableDataInfo.success(examVOList, total);
    }

    private void assembleExamVOList(List<ExamVO> examVOList) {
        //1. 拿到用户报名的竞赛列表（List<Long>）
        Long userId = ThreadLocalUtils.get(Constants.USER_ID, Long.class);
        List<Long> userExamIdList = examCacheManager.getAllUserExamList(userId);
        //如果为空，证明用户没有报名过任何竞赛，直接返回（enter默认false未报名）
        if (CollectionUtil.isEmpty(userExamIdList)) {
            return;
        }
        for (ExamVO examVO : examVOList) {
            if (userExamIdList.contains(examVO.getExamId())) {
                examVO.setEnter(true);
            }
        }
    }

}
