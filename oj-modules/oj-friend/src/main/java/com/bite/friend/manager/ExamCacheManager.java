package com.bite.friend.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.enums.ExamListType;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.redis.service.RedisService;
import com.bite.common.security.exception.ServiceException;
import com.bite.friend.domain.exam.Exam;
import com.bite.friend.domain.exam.ExamQuestion;
import com.bite.friend.domain.exam.ExamRankInfo;
import com.bite.friend.domain.exam.dto.ExamQueryDTO;
import com.bite.friend.domain.exam.dto.ExamRankDTO;
import com.bite.friend.domain.exam.vo.ExamRankVO;
import com.bite.friend.domain.exam.vo.ExamVO;
import com.bite.friend.domain.message.vo.MessageTextVO;
import com.bite.friend.domain.user.UserExam;
import com.bite.friend.mapper.exam.ExamMapper;
import com.bite.friend.mapper.exam.ExamQuestionMapper;
import com.bite.friend.mapper.user.UserExamMapper;
import com.bite.friend.mapstruct.ExamVoToExamMapper;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private ExamQuestionMapper examQuestionMapper;


    public Long getExamListSize(Integer examListType, Long userId) {
        String examListKey = getExamListKey(examListType, userId);
        return redisService.getListSize(examListKey);
    }

    public Long getExamQuestionListSize(Long examId) {
        return redisService.getListSize(getExamQuestionListKey(examId));
    }

    public Long getExamRankListSize(Long examId) {
        return redisService.getListSize(getExamRankListKey(examId));
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
     * TODO 有，因为第一次是分页查询，第二次不是分页查询，一次性刷新一次缓存
     * 有必要将examVOList转换为examList吗
     *
     * 目前实现：将service的数据库查询结果直接缓存
     */
    public void refreshCache(List<ExamVO> examVOList, Integer examListType, Long userId) {
//
//        List<Exam> examList = new ArrayList<>();
//        if (ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(examListType)) {
//            //查询未完赛的竞赛列表
//            examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
//                    .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
//                    .gt(Exam::getEndTime, LocalDateTime.now())
//                    .eq(Exam::getStatus, Constants.TRUE)
//                    .orderByDesc(Exam::getCreateTime));
//        } else if (ExamListType.EXAM_HISTORY_LIST.getValue().equals(examListType)) {
//            //查询历史竞赛
//            examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
//                    .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
//                    .le(Exam::getEndTime, LocalDateTime.now())
//                    .eq(Exam::getStatus, Constants.TRUE)
//                    .orderByDesc(Exam::getCreateTime));
//        } else if (ExamListType.USER_EXAM_LIST.getValue().equals(examListType)) {
//            List<ExamVO> examVOList = userExamMapper.selectUserExamList(userId);
//            examList = BeanUtil.copyToList(examVOList, Exam.class);
//        }
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

    public List<ExamRankVO> getExamRankVOListFromCache(ExamRankDTO examRankDTO) {
        int start = (examRankDTO.getPageNum() - 1) * examRankDTO.getPageSize();
        int end = start + examRankDTO.getPageSize() - 1;
        String examRankListKey = getExamRankListKey(examRankDTO.getExamId());
        List<ExamRankVO> examRankVOList;
        List<ExamRankInfo> examRankInfoList = redisService.getCacheListByRange(examRankListKey, start, end,
                ExamRankInfo.class);
        if (CollectionUtil.isEmpty(examRankInfoList)) {
            examRankVOList = getExamRankVOListFromDB(examRankDTO);
            refreshExamRankListCache(examRankDTO.getExamId());
        } else {
            examRankVOList = BeanUtil.copyToList(examRankInfoList, ExamRankVO.class);
        }
        return examRankVOList;
    }

    public void cacheUserExamList(Long userId, Long examId) {
        String userExamListKey = getUserExamListKey(userId);
        redisService.leftPushForList(userExamListKey, examId);
    }

    public List<Long> getAllUserExamList(Long userId) {
        String userExamListKey = getUserExamListKey(userId);
        List<Long> userExamIdList  = redisService.getCacheListByRange(userExamListKey, 0, -1, Long.class);
        //缓存未命中
        if (CollectionUtil.isNotEmpty(userExamIdList)) {
            return userExamIdList;
        }
        //查询数据库
        List<UserExam> userExamList = userExamMapper.selectList(new LambdaQueryWrapper<UserExam>()
                .eq(UserExam::getUserId, userId));
        if (CollectionUtil.isEmpty(userExamList)) {
            //数据库中无数据
            return null;
        }
        //刷新缓存，refreshCache方法需要List<ExamVO>参数，而UserExam与ExamVO的属性差别过大，
        // 并不能支撑转换，因此此处需要查询数据库（老师代码依旧是在refreshCache方法中统一再查数据库，而我的代码并没有统一
        // 再查，因此本质上老师的代码也是额外查询一次，只不过位置不同，后续如果还需要大调整可以考虑重构为老师的写法）
        List<ExamVO> examVOList = userExamMapper.selectUserExamList(userId);
        //如果用户报名过任一竞赛，就刷新 user:exam:list:用户id 缓存
        if (CollectionUtil.isNotEmpty(examVOList)) {
            refreshCache(examVOList, ExamListType.USER_EXAM_LIST.getValue(), userId);
        }
        return userExamList.stream().map(UserExam::getExamId).toList();
    }

    public void refreshExamQuestionListCache(Long examId) {
        List<ExamQuestion> examQuestionList = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .select(ExamQuestion::getQuestionId)
                .eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getQuestionOrder));
        if (CollectionUtil.isEmpty(examQuestionList)) {
            return;
        }
        String examQuestionListKey = getExamQuestionListKey(examId);
        List<Long> examQuestionIdList = examQuestionList.stream().map(ExamQuestion::getQuestionId).toList();
        //TODO 删除 redisService.deleteObject(getExamListKey(examListType, userId));
        redisService.rightPushAll(examQuestionListKey, examQuestionIdList);
        //这里竞赛题目的获取高峰大概率在当天（答题/练习），隔天竞赛结束，用户练习的需求大概率减少，因此节省缓存
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        redisService.expire(examQuestionListKey, seconds, TimeUnit.SECONDS);
    }

    /**
     * 此方法一开始可以直接不分页查询一次数据库，将更多的数据写入缓存，但是，如果缓存只是部分残缺而将数据全部写入缓存，
     * 数据就可能重复。正常情况下，对于该缓存的刷新都是全量的，所以不会出现问题
     *
     * 如果缓存刷新部分数据，就可能出现缓存数据错误问题，具体来说：
     * 原来排名靠后的一页数据可能就会因为前端先到该页而将这部分数据放在缓存list靠前的位置，造成顺序混乱问题，因此：
     * 这里还是全量查询刷新比价好；
     * @param examId
     */
    public void refreshExamRankListCache(Long examId) {
        List<ExamRankVO> examRankVOList = userExamMapper.selectExamRankList(examId);
        if (CollectionUtil.isEmpty(examRankVOList)) {
            return;
        }
        List<ExamRankInfo> examRankInfoList = BeanUtil.copyToList(examRankVOList, ExamRankInfo.class);
        redisService.deleteObject(getExamRankListKey(examId));
        redisService.rightPushAll(getExamRankListKey(examId), examRankInfoList);
    }

    public Long getFirstQuestion(Long examId) {
        return redisService.indexForList(getExamQuestionListKey(examId), 0, Long.class);
    }

    public Long preQuestion(Long examId, Long questionId) {
        String examQuestionListKey = getExamQuestionListKey(examId);
        Long index = redisService.indexOfForList(examQuestionListKey, questionId);
        if (index == 0) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_FIRST_QUESTION);
        }
        return redisService.indexForList(examQuestionListKey, index - 1, Long.class);
    }

    public Long nextQuestion(Long examId, Long questionId) {
        String examQuestionListKey = getExamQuestionListKey(examId);
        Long index = redisService.indexOfForList(examQuestionListKey, questionId);
        Long lastIndex = getExamQuestionListSize(examId) - 1;
        if (index == lastIndex) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_LAST_QUESTION);
        }
        return redisService.indexForList(examQuestionListKey, index + 1, Long.class);
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

    private List<ExamRankVO> getExamRankVOListFromDB(ExamRankDTO examRankDTO) {
        PageHelper.startPage(examRankDTO.getPageNum(), examRankDTO.getPageSize());
        return userExamMapper.selectExamRankList(examRankDTO.getExamId());
    }

    private List<ExamVO> assembleExamVOList(List<Long> examIdList) {
        if (CollectionUtil.isEmpty(examIdList)) {
            return null;
        }
        //构造ExamDetailKeyList
        List<String> examDetailKeyList = examIdList.stream().map(this::getExamDetailKey).toList();
        List<ExamVO> examVOList = redisService.multiGet(examDetailKeyList, ExamVO.class);
        CollUtil.removeNull(examVOList);
        if (CollectionUtil.isEmpty(examVOList) || examVOList.size() != examIdList.size()) {
            return null;
        }
        return examVOList;
    }

    private String getUserExamListKey(Long userId) {
        return CacheConstants.USER_EXAM_LIST_KEY_PREFIX + userId;
    }

    private String getExamQuestionListKey(Long examId) {
        return CacheConstants.EXAM_QUESTION_LIST_KEY_PREFIX + examId;
    }

    private String getExamRankListKey(Long examId) {
        return CacheConstants.EXAM_RANK_LIST_KEY_PREFIX + examId;
    }
}
