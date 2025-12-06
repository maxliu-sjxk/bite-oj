package com.bite.friend.service.user.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.domain.PageQueryDTO;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.common.core.utils.ThreadLocalUtils;
import com.bite.friend.domain.message.vo.MessageTextVO;
import com.bite.friend.manager.MessageCacheManager;
import com.bite.friend.mapper.message.MessageTextMapper;
import com.bite.friend.service.user.IUserMessageService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserMessageServiceImpl implements IUserMessageService {


    @Autowired
    private MessageCacheManager messageCacheManager;

    @Autowired
    private MessageTextMapper messageTextMapper;

    @Override
    public TableDataInfo list(PageQueryDTO pageQueryDTO) {
        Long userId = ThreadLocalUtils.get(Constants.USER_ID, Long.class);
        //查询缓存中竞赛list的大小
        Long total = messageCacheManager.getUserMessageListSize(userId);
        List<MessageTextVO> messageTextVOList;
        if (total == null || total <= 0L) {
            //缓存未命中，直接查询数据库
            PageHelper.startPage(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
            messageTextVOList = messageTextMapper.selectUserMessageList(userId);
            //刷新缓存
            messageCacheManager.refreshCache(userId);
            total = new PageInfo<>(messageTextVOList).getTotal();
        } else {
            //缓存命中，直接查询缓存
            messageTextVOList = messageCacheManager.getMessageTextVOListFromCache(pageQueryDTO, userId);
            total = messageCacheManager.getUserMessageListSize(userId);
        }
        if (CollectionUtil.isEmpty(messageTextVOList)) {
            return TableDataInfo.empty();
        }
        return TableDataInfo.success(messageTextVOList, total);
    }
}
