package com.bite.job.service;

import com.bite.job.domain.message.Message;

import java.util.List;

public interface IMessageService {


    boolean batchInsert(List<Message> messageList);
}
