package com.bite.common.security.service;

import cn.hutool.core.lang.UUID;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.JwtConstants;
import com.bite.common.core.enums.UserIdentity;
import com.bite.common.redis.service.RedisService;
import com.bite.common.core.domain.LoginUser;
import com.bite.common.core.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    @Autowired
    private RedisService redisService;

    public String createTokenAndCache(Long userId, String secret, UserIdentity identity) {
        //生成UUID
        String userKey = UUID.fastUUID().toString();

        //生成Token
        String token = createToken(userId, secret, userKey);

        //缓存
        cacheLoginUser(userKey, identity);

        return token;
    }

    private void cacheLoginUser(String userKey, UserIdentity identity) {
        //key-> jwt:token:uuid
        //value<string>-> LoginUser{ identity:int , …… }
        String key = CacheConstants.LOGIN_TOKEN_KEY_PREFIX + userKey;
        LoginUser loginUser = new LoginUser();
        loginUser.setIdentity(identity.getValue());
        redisService.setCacheObject(key, loginUser, CacheConstants.EXP, TimeUnit.MINUTES);
    }

    private String createToken(Long userId, String secret, String userKey) {
        //载荷存储：唯一标识（userId + UUID）
        //考虑到：后续查看jwt是否过期是通过查询缓存中是否存在jwt:token的key，因此验证逻辑时需要拼接出key，用到uuid
        //因此，将uuid也存储在载荷中

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.LOGIN_USER_ID, userId);
        claims.put(JwtConstants.LOGIN_USER_KEY, userKey);
        return JwtUtils.createToken(claims, secret);
    }
}
