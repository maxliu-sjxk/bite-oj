package com.bite.common.security.service;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.JwtConstants;
import com.bite.common.core.enums.UserIdentity;
import com.bite.common.redis.service.RedisService;
import com.bite.common.core.domain.LoginUser;
import com.bite.common.core.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TokenService {

    @Autowired
    private RedisService redisService;


    /**
     * 创建token并缓存到Redis
     * 初次用于用户登录接口
     * @param userId
     * @param secret
     * @param identity
     * @param nickName
     * @return
     */
    public String createTokenAndCache(Long userId, String secret, UserIdentity identity, String nickName) {
        //生成UUID
        String userKey = UUID.fastUUID().toString();

        //生成Token
        String token = createToken(userId, secret, userKey);

        //缓存
        cacheLoginUser(userKey, identity, nickName);

        return token;
    }

    /**
     * 缓存登录用户信息
     * @param userKey
     * @param identity
     * @param nickName
     */
    private void cacheLoginUser(String userKey, UserIdentity identity, String nickName) {
        //key-> jwt:token:uuid
        //value<string>-> LoginUser{ identity:int , …… }
        String key = CacheConstants.LOGIN_TOKEN_KEY_PREFIX + userKey;
        LoginUser loginUser = new LoginUser();
        loginUser.setIdentity(identity.getValue());
        loginUser.setNickName(nickName);
        redisService.setCacheObject(key, loginUser, CacheConstants.EXP, TimeUnit.MINUTES);
    }

    /**
     * 创建token
     * @param userId
     * @param secret
     * @param userKey
     * @return
     */
    private String createToken(Long userId, String secret, String userKey) {
        //载荷存储：唯一标识（userId + UUID）
        //考虑到：后续查看jwt是否过期是通过查询缓存中是否存在jwt:token的key，因此验证逻辑时需要拼接出key，用到uuid
        //因此，将uuid也存储在载荷中

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.LOGIN_USER_ID, userId);
        claims.put(JwtConstants.LOGIN_USER_KEY, userKey);
        return JwtUtils.createToken(claims, secret);
    }

    /**
     * 延长token有效期
     * @param token
     * @param secret
     */
    public void extendExpire(String token, String secret) {
        String userKey = getUserKey(token, secret);
        if (userKey == null) {
            return;
        }
        //判断剩余时间是否满足延长阈值
        if (!StrUtil.isEmpty(userKey) && redisService.getExpire(getTokenKey(userKey), TimeUnit.MINUTES) < CacheConstants.REFRESH_TIME) {
            redisService.expire(getTokenKey(userKey), CacheConstants.EXP, TimeUnit.MINUTES);
        }
    }

    /**
     * 解析 token获取 userKey（uuid）
     * @param token
     * @param secret
     * @return
     */
    public String getUserKey(String token, String secret) {
        //解析token获取载荷
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token, secret);
            if (claims == null) {
                log.error("token解析失败: {}", token);
                return null;
            }
        } catch (Exception e) {
            log.error("token解析失败: {}, 异常", token, e);
            return null;
        }
        //解析载荷获取userKey
        return JwtUtils.getUserKey(claims);
    }

    /**
     * 根据userKey拼接Redis中的key
     * @param userKey
     * @return
     */
    private String getTokenKey(String userKey) {
        return CacheConstants.LOGIN_TOKEN_KEY_PREFIX + userKey;
    }

    /**
     * 获取到Redis中存储的value
     * 初次用于获取用户信息（昵称）
     * @param token
     * @param secret
     * @return
     */
    public LoginUser getLoginUser(String token, String secret) {
        String userKey = getUserKey(token, secret);
        if (userKey == null) {
            return null;
        }
        return redisService.getCacheObject(getTokenKey(userKey), LoginUser.class);
    }

    public boolean deleteToken(String token, String secret) {
        String userKey = getUserKey(token, secret);
        if (userKey == null) {
            return false;
        }
        return redisService.deleteObject(getTokenKey(userKey));
    }
}
