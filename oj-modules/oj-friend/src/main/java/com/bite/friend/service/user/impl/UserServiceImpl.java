package com.bite.friend.service.user.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.constants.HttpConstants;
import com.bite.common.core.domain.LoginUser;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.vo.LoginUserVO;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.core.enums.UserIdentity;
import com.bite.common.core.enums.UserStatus;
import com.bite.common.core.utils.ThreadLocalUtils;
import com.bite.common.message.service.Mail;
import com.bite.common.redis.service.RedisService;
import com.bite.common.security.exception.ServiceException;
import com.bite.common.security.service.TokenService;
import com.bite.friend.domain.user.User;
import com.bite.friend.domain.user.dto.UserDTO;
import com.bite.friend.domain.user.dto.UserUpdateDTO;
import com.bite.friend.domain.user.vo.UserVO;
import com.bite.friend.manager.UserCacheManager;
import com.bite.friend.mapper.user.UserMapper;
import com.bite.friend.service.user.IUserService;
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

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserCacheManager userCacheManager;

    @Value("${captcha.send-limit:3}")
    private Integer sendLimit;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${captcha.is-send:false}")
    private boolean isSend;


    /**
     * 限制：
     * 1. 验证码有效期为5分钟
     * 2. 验证码获取不能太频繁，1分钟只能获取一次
     * 3. 验证码每天限获取50（可动态调整）次
     *
     * 逻辑：
     * 判断此次验证码请求是否为频繁（两次请求间隔小于60s）
     * 判断当天获取验证码是否超过50次
     * 生成随机6位验证码
     * 缓存验证码并设置过期时间5分钟
     * 向用户邮箱发送验证码
     * 当天验证码请求计数加一
     * 判断此时是否为当天第一次获取验证码，如果是则设置key的过期时间，为当前时间与零点时间差
     *
     * 其他：
     * 增加isSend开关，用于控制是否真的发送邮件，目的：方便测试
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
        String code = isSend ? RandomUtil.randomNumbers(6) : Constants.DEFAULT_CODE;
        //缓存验证码并设置过期时间5分钟
        redisService.setCacheObject(emailCodeKey, code, CacheConstants.EMAIL_CODE_EXP, TimeUnit.MINUTES);
        if (isSend) {
            try {
                mailService.send(userDTO.getEmail(), Constants.MAIL_SUBJECT, buildContent(code));
            } catch (Exception e) {
                log.error("发送邮件失败", e);
                throw new ServiceException(ResultCode.FAILED_EMAIL_SEND);
            }
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

    /**
     * 流程：
     * 1. 进行验证码验证：验证码缓存是否存在（有效），验证码是否正确
     * 2. 查询数据库，如果用户不存在（新用户），则先创建用户（数据库插入）
     * 3. 创建并缓存，返回token
     * @param email
     * @param code
     * @return
     */
    @Override
    public String codeLogin(String email, String code) {
        checkCode(email, code);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            //新用户
            user = new User();
            user.setEmail(email);
            user.setStatus(UserStatus.NORMAL.getValue());
            userMapper.insert(user);
        }
        String token = tokenService.createTokenAndCache(user.getUserId(),
                secret, UserIdentity.ORDINARY, user.getNickName(), user.getHeadImage());
        return token;
    }

    @Override
    public R<Void> logout(String token) {
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        if (!tokenService.deleteToken(token, secret)) {
            return R.fail();
        }
        return R.ok();
    }

    @Override
    public R<LoginUserVO> info(String token) {
        //去除前缀
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        //获取用户昵称
        LoginUser loginUser = tokenService.getLoginUser(token, secret);
        if (loginUser == null) {
            return R.fail();
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setNickName(loginUser.getNickName());
        loginUserVO.setHeadImage(loginUser.getHeadImage());
        return R.ok(loginUserVO);
    }

    @Override
    public R<UserVO> detail() {
        Long userId = ThreadLocalUtils.get(Constants.USER_ID, Long.class);
        if (userId == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        UserVO userVO = userCacheManager.getUserCacheById(userId);
        if (userVO == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        return R.ok(userVO);
    }

    @Override
    public int edit(UserUpdateDTO userUpdateDTO) {
        Long userId = ThreadLocalUtils.get(Constants.USER_ID, Long.class);
        if (userId == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        user.setNickName(userUpdateDTO.getNickName());
        user.setSex(userUpdateDTO.getSex());
        user.setSchoolName(userUpdateDTO.getSchoolName());
        user.setMajorName(userUpdateDTO.getMajorName());
        user.setPhone(userUpdateDTO.getPhone());
        user.setEmail(userUpdateDTO.getEmail());
        user.setWechat(userUpdateDTO.getWechat());
        user.setIntroduce(userUpdateDTO.getIntroduce());
        userCacheManager.refreshUserCache(user);
        tokenService.refreshLoginUser(user.getNickName(), user.getHeadImage(),
                ThreadLocalUtils.get(Constants.USER_KEY, String.class));
        return userMapper.updateById(user);
    }

    private void checkCode(String email, String code) {
        String emailCodeKey = getEmailCodeKey(email);
        String cacheCode = redisService.getCacheObject(emailCodeKey, String.class);
        //验证码缓存不存在
        if (StrUtil.isEmpty(cacheCode)) {
            throw new ServiceException(ResultCode.FAILED_INVALID_CODE);
        }
        //验证码错误
        if (!code.equals(cacheCode)) {
            throw new ServiceException(ResultCode.FAILED_ERROR_CODE);
        }
        redisService.deleteObject(emailCodeKey);
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
