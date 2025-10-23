package com.bite.friend.service.exam.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.bite.common.core.domain.TableDataInfo;
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
        return TableDataInfo.success(examVOList, total);
    }

}
