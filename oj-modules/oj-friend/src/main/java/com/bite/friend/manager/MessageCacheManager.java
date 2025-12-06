package com.bite.friend.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.domain.PageQueryDTO;
import com.bite.common.redis.service.RedisService;
import com.bite.friend.domain.exam.vo.ExamVO;
import com.bite.friend.domain.message.vo.MessageTextVO;
import com.bite.friend.mapper.message.MessageTextMapper;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MessageCacheManager {

    @Autowired
    private RedisService redisService;

    @Autowired
    private MessageTextMapper messageTextMapper;

    public Long getUserMessageListSize(Long userId) {
        return redisService.getListSize(getUserMessageListKey(userId));
    }

    public void refreshCache(Long userId) {
        List<MessageTextVO> messageTextVOList = messageTextMapper.selectUserMessageList(userId);
        if (CollectionUtil.isEmpty(messageTextVOList)) {
            return;
        }
        List<Long> textIdList = new ArrayList<>();
        Map<String, MessageTextVO> messageTextVOMap = new HashMap<>();
        for (MessageTextVO messageTextVO : messageTextVOList) {
            Long textId = messageTextVO.getTextId();
            textIdList.add(textId);
            messageTextVOMap.put(getMessageDetailKey(textId), messageTextVO);
        }
        redisService.deleteObject(getUserMessageListKey(userId));
        redisService.rightPushAll(getUserMessageListKey(userId), textIdList);
        redisService.multiSet(messageTextVOMap);
    }

    public List<MessageTextVO> getMessageTextVOListFromCache(PageQueryDTO pageQueryDTO, Long userId) {
        int start = (pageQueryDTO.getPageNum() - 1) * pageQueryDTO.getPageSize();
        int end = start + pageQueryDTO.getPageSize() - 1;
        List<Long> textIdList = redisService.getCacheListByRange(getUserMessageListKey(userId), start, end, Long.class);
        List<MessageTextVO> messageTextVOList = assembleMessageTextVOList(textIdList);
        if (CollectionUtil.isEmpty(messageTextVOList)) {
            messageTextVOList = getMessageVOListFromDB(pageQueryDTO, userId);
            refreshCache(userId);
        }
        return messageTextVOList;
    }

    private List<MessageTextVO> getMessageVOListFromDB(PageQueryDTO pageQueryDTO, Long userId) {
        PageHelper.startPage(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        return messageTextMapper.selectUserMessageList(userId);
    }

    private List<MessageTextVO> assembleMessageTextVOList(List<Long> textIdList) {
        List<String> messageDetailKeyList = textIdList.stream().map(this::getMessageDetailKey).toList();
        List<MessageTextVO> messageTextVOList = redisService.multiGet(messageDetailKeyList, MessageTextVO.class);
        CollUtil.removeNull(messageDetailKeyList);
        if (CollectionUtil.isEmpty(messageTextVOList) || messageTextVOList.size() != textIdList.size()) {
            return null;
        }
        return messageTextVOList;
    }


    public String getUserMessageListKey(Long userId) {
        return CacheConstants.USER_MESSAGE_LIST_KEY_PREFIX + userId;
    }

    public String getMessageDetailKey(Long textId) {
        return CacheConstants.MESSAGE_DETAIL_KEY_PREFIX + textId;
    }

}
