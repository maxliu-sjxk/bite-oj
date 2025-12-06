package com.bite.job.handler;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.Constants;
import com.bite.common.redis.service.RedisService;
import com.bite.job.domain.exam.Exam;
import com.bite.job.domain.exam.ExamRankInfo;
import com.bite.job.domain.message.Message;
import com.bite.job.domain.message.MessageText;
import com.bite.job.domain.message.vo.MessageTextVO;
import com.bite.job.domain.user.UserScore;
import com.bite.job.mapper.exam.ExamMapper;
import com.bite.job.mapper.user.UserExamMapper;
import com.bite.job.mapper.user.UserSubmitMapper;
import com.bite.job.service.IMessageService;
import com.bite.job.service.IMessageTextService;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExamXxlJob {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private UserSubmitMapper userSubmitMapper;

    @Autowired
    private RedisService redisService;

    @Resource(name = "messageTextServiceImpl")
    private IMessageTextService messageTextService;

    @Resource(name = "messageServiceImpl")
    private IMessageService messageService;

    @Autowired
    private UserExamMapper userExamMapper;

    /**
     * 完赛竞赛移入历史竞赛
     */
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


    /**
     * 竞赛结果处理定时任务（设置凌晨1点执行）
     *
     * 流程：
     * 1. 库中查询所有前一天结束的所有已发布的竞赛（tb_exam）
     * 2. 库中查询上一步得到的所有竞赛相关的提交记录（tb_user_submit）并根据竞赛分组
     * 3. 遍历每个竞赛，构造消息入库
     * 4. 将结果同步给缓存
     */
    @XxlJob("examResultOrganizeHandler")
    public void examResultOrganizeHandler() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        List<Exam> examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .select(Exam::getExamId, Exam::getTitle)
                .eq(Exam::getStatus, Constants.TRUE)
                .ge(Exam::getEndTime, yesterday)
                .le(Exam::getEndTime, now));
        if (CollectionUtil.isEmpty(examList)) {
            return;
        }
        Set<Long> examIdSet = examList.stream().map(Exam::getExamId).collect(Collectors.toSet());
        List<UserScore> userScoreList = userSubmitMapper.selectUserScoreList(examIdSet);
        Map<Long, List<UserScore>> userScoreMap = userScoreList.stream()
                .collect(Collectors.groupingBy(UserScore::getExamId));

        generateMessage(examList, userScoreMap);
    }

    private void generateMessage(List<Exam> examList, Map<Long, List<UserScore>> userScoreMap) {
        List<MessageText> messageTextList = new ArrayList<>();
        List<Message> messageList = new ArrayList<>();
        for (Exam exam : examList) {
            Long examId = exam.getExamId();
            List<UserScore> userScoreList = userScoreMap.get(examId);
            int totalUser = userScoreList.size();
            int examRank = 1;
            for (UserScore userScore : userScoreList) {
                userScore.setExamRank(examRank);
                MessageText messageText = MessageText.build(buildMessageTitle(exam.getTitle()),
                        buildMessageContent(exam.getTitle(), totalUser, examRank));
                messageText.setCreateBy(Constants.SYSTEM_USER_ID);
                messageTextList.add(messageText);

                Message message = Message.build(Constants.SYSTEM_USER_ID, userScore.getUserId());
                message.setCreateBy(Constants.SYSTEM_USER_ID);
                messageList.add(message);
                examRank++;
            }
            userExamMapper.updateUserScoreAndRank(userScoreList);
            List<ExamRankInfo> examRankInfoList = BeanUtil.copyToList(userScoreList, ExamRankInfo.class);
            redisService.rightPushAll(getExamRankListKey(examId), examRankInfoList);
        }
        messageTextService.batchInsert(messageTextList);
        //循环内部实现：redis批量插入的数据 + 欲插入数据库的Message数据的textId字段填充
        Map<String, MessageTextVO> messageDetailMap = new HashMap<>();
        for (int i = 0; i < messageTextList.size(); i++) {
            MessageText messageText = messageTextList.get(i);
            MessageTextVO messageTextVO = new MessageTextVO();
            BeanUtil.copyProperties(messageText, messageTextVO);
            messageDetailMap.put(getMessageDetailKey(messageText.getTextId()), messageTextVO);
            //将最后的textId进行填充
            messageList.get(i).setTextId(messageText.getTextId());
        }
        messageService.batchInsert(messageList);
        //同步缓存
        //user:message:list:用户ID -> [textIds]
        Map<Long, List<Message>> messageMap = messageList.stream()
                .collect(Collectors.groupingBy(Message::getRecId));
        //遍历Map
        for (Map.Entry<Long, List<Message>> entry : messageMap.entrySet()) {
            Long userId = entry.getKey();
            List<Long> textIdList = entry.getValue().stream().map(Message::getTextId).toList();
            redisService.rightPushAll(getUserMessageListKey(userId), textIdList);
        }
        //message:detail:消息内容ID -> MessageTextVO
        redisService.multiSet(messageDetailMap);
    }

    private String buildMessageTitle(String examTitle) {
        return Constants.MESSAGE_TITLE_PREFIX + examTitle + Constants.MESSAGE_TITLE_SUFFIX;
    }

    private String buildMessageContent(String examTitle, int totalUser, int examRank) {
        return "您所参与的竞赛：【" + examTitle + "】已结束。" +
                "本次竞赛共有" + totalUser + "位用户参与，您的排名为：" + examRank + "位。";
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

    private String getUserMessageListKey(Long userId) {
        return CacheConstants.USER_MESSAGE_LIST_KEY_PREFIX + userId;
    }

    private String getMessageDetailKey(Long textId) {
        return CacheConstants.MESSAGE_DETAIL_KEY_PREFIX + textId;
    }

    private String getExamRankListKey(Long examId) {
        return CacheConstants.EXAM_RANK_LIST_KEY_PREFIX + examId;
    }


}
