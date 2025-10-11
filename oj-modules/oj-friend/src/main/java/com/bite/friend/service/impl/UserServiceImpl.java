package com.bite.friend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.message.service.Mail;
import com.bite.common.redis.service.RedisService;
import com.bite.common.security.exception.ServiceException;
import com.bite.friend.domain.dto.UserDTO;
import com.bite.friend.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@RefreshScope
@Service
@Slf4j
public class UserServiceImpl implements IUserService {

    @Autowired
    private Mail mailService;

    @Autowired
    private RedisService redisService;


    @Value("${captcha.send-limit}")
    private Integer sendLimit;


    /**
     * 限制：
     * 1. 验证码有效期为5分钟
     * 2. 验证码获取不能太频繁，1分钟只能获取一次
     * 3. 验证码每天限获取50（可动态调整）次
     * 逻辑：
     * 判断此次验证码请求是否为频繁（两次请求间隔小于60s）
     * 判断当天获取验证码是否超过50次
     * 生成随机6位验证码
     * 缓存验证码并设置过期时间5分钟
     * 向用户邮箱发送验证码
     * 当天验证码请求计数加一
     * 判断此时是否为当天第一次获取验证码，如果是则设置key的过期时间，为当前时间与零点时间差
     * @param userDTO
     * @return
     */
    @Override
    public boolean sendCode(UserDTO userDTO) {
        //生成随机6位验证码
        String emailCodeKey = getEmailCodeKey(userDTO.getEmail());
        Long expire = redisService.getExpire(emailCodeKey, TimeUnit.SECONDS);
        //如果存在过期时间（即上次验证码有效期未过）并且有效期未过1分钟，则判定频繁获取验证码
        if (expire != null && (CacheConstants.EMAIL_CODE_EXP * 60 - expire) < CacheConstants.MIN_SEND_INTERVAL) {
            throw new ServiceException(ResultCode.FAILED_FREQUENT_REQUEST);
        }
        //判断当天验证码数量是否超过sendLimit次
        String codeTimesKey = getCodeTimesKey(userDTO.getEmail());
        Long sendTimes = redisService.getCacheObject(codeTimesKey, Long.class);
        if (sendTimes != null && sendTimes >= sendLimit) {
            throw new ServiceException(ResultCode.FAILED_TIMES_LIMIT);
        }
        String code = RandomUtil.randomNumbers(6);
        //缓存验证码并设置过期时间5分钟
        redisService.setCacheObject(emailCodeKey, code, CacheConstants.EMAIL_CODE_EXP, TimeUnit.MINUTES);
        try {
            mailService.send(userDTO.getEmail(), Constants.MAIL_SUBJECT, buildContent(code));
        } catch (Exception e) {
            log.error("发送邮件失败", e);
            throw new ServiceException(ResultCode.FAILED_EMAIL_SEND);
        }
        //发送成功后，计数加一
        redisService.increment(codeTimesKey);
        //如果之前的sendTimes的值为null，意味着本次为当天首次发送，需要设置该key的过期时间
        //过期时间是动态的，即当前时间与零点的时间差
        if (sendTimes == null) {
            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
            redisService.expire(codeTimesKey, seconds, TimeUnit.SECONDS);
        }
        return true;
    }

    private String getCodeTimesKey(String email) {
        return CacheConstants.CODE_TIMES_KEY_PREFIX + email;
    }

    private String getEmailCodeKey(String email) {
        return CacheConstants.EMAIL_CODE_KEY_PREFIX + email;
    }

    private String buildContent(String code) {
        StringBuilder content = new StringBuilder();
        content.append("尊敬的用户，您好！").append("<br/>");
        content.append("您正在进行OJ系统的登录/注册操作，需完成验证码验证以保障账号安全。<br/>");
        content.append("您的验证码为：").append(code).append("<br/>");
        content.append("验证码有效期为5分钟，请在有效期内完成验证。<br/>");
        content.append("若您未发起此操作，可能是他人误填您的邮箱，请忽略本邮件或及时检查账号安全。<br/>");
        content.append("感谢您对OJ系统的使用！");
        return content.toString();
    }
}
