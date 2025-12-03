package com.bite.judge.rabbit;

import com.bite.api.domain.dto.JudgeSubmitDTO;
import com.bite.common.core.constants.RabbitMQConstants;
import com.bite.judge.service.IJudgeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * TODO 使用高级特性，比如消费者手动确认
 */
@Component
@Slf4j
public class JudgeConsumer {

    @Resource(name = "judgeServiceImpl")
    private IJudgeService judgeService;

    @RabbitListener(queues = RabbitMQConstants.OJ_WORK_QUEUE)
    public void consume(JudgeSubmitDTO judgeSubmitDTO) {
        log.info("收到消息为: {}", judgeSubmitDTO);
        judgeService.doJudgeJavaCode(judgeSubmitDTO);
    }
}
