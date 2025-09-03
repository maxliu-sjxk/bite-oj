package com.bite.gateway.filter;

import cn.hutool.core.text.AntPathMatcher;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.HttpConstants;
import com.bite.common.core.constants.JwtConstants;
import com.bite.common.core.domain.LoginUser;
import com.bite.common.core.domain.R;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.core.enums.UserIdentity;
import com.bite.common.redis.service.RedisService;
import com.bite.common.core.utils.JwtUtils;
import com.bite.gateway.config.IgnoreWhiteConfig;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {


    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IgnoreWhiteConfig ignoreWhiteConfig;

    /**
     * 身份认证
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求路径，判断是否白名单，白名单直接跳过认证
        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().getPath();
        if (matches(url, ignoreWhiteConfig.getWhites())) {
            return chain.filter(exchange);
        }
        String token = getToken(request);
        if (StrUtil.isEmpty(token)) {
            return unauthorizedResponse(exchange, "令牌不能为空！");
        }

        Claims claims;
        //验证token拿到载荷
        try {
            claims = JwtUtils.parseToken(token, secret);
            if (claims == null) {
                return unauthorizedResponse(exchange, "令牌验证失败！");
            }
        } catch (Exception e) {
            return unauthorizedResponse(exchange, "令牌验证失败！");
        }
        //解析载荷信息(userId、uuid)，根据uuid拼接key并检查缓存是否存在，检查是否存在userId，即信息是否完整
        String userKey = JwtUtils.getUserKey(claims);
        boolean isOverdue = redisService.hasKey(getTokenKey(userKey));
        if (!isOverdue) {
            return unauthorizedResponse(exchange, "登录状态已过期！");
        }
        String userId = JwtUtils.getUserId(claims);
        if (StrUtil.isEmpty(userId)) {
            return unauthorizedResponse(exchange, "令牌验证失败！");
        }

        //token正确且未过期，验证Redis缓存中的权限信息
        LoginUser user = redisService.getCacheObject(getTokenKey(userKey), LoginUser.class);

        //如果请求C端，则验证C端身份信息，确保身份为：ORDINARY
        if (url.contains(HttpConstants.FRIEND_URL_PREFIX) && !UserIdentity.ORDINARY.getValue().equals(user.getIdentity())) {
            return unauthorizedResponse(exchange, "您没有权限访问！");
        }

        //如果请求A端，则验证A端身份信息，确保身份为：ADMIN
        if (url.contains(HttpConstants.SYSTEM_URL_PREFIX) && !UserIdentity.ADMIN.getValue().equals(user.getIdentity())) {
            return unauthorizedResponse(exchange, "您没有权限访问！");
        }
        return chain.filter(exchange);
    }

    /**
     * 根据传入的userKey，拼接并返回 Redis中存储的 key
     * @param userKey
     * @return
     */
    private String getTokenKey(String userKey) {
        return CacheConstants.LOGIN_TOKEN_KEY_PREFIX + userKey;
    }

    /**
     * 处理鉴权失败
     * @param exchange
     * @param msg
     * @return
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg) {
        log.error("[鉴权异常处理] 请求路径:{}", exchange.getRequest().getPath());
        return webFluxResponseWriter(exchange.getResponse(), msg, ResultCode.FAILED_UNAUTHORIZED.getCode());
    }

    /**
     * 响应
     * @param response
     * @param msg
     * @param code
     * @return
     */
    private Mono<Void> webFluxResponseWriter(ServerHttpResponse response, String msg, int code) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        R<?> result = R.fail(code, msg);
        DataBuffer dataBuffer = response.bufferFactory().wrap(JSON.toJSONString(result).getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }

    /**
     * 判断 url是否与白名单匹配
     * @param url
     * @param patternList
     * @return
     */
    private boolean matches(String url, List<String> patternList) {
        if (StrUtil.isEmpty(url) || CollectionUtils.isEmpty(patternList)) {
            return false;
        }
        for (String pattern : patternList) {
            if (isMatch(url, pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 具体匹配，支持 * ** ？等
     * @param url
     * @param pattern
     * @return
     */
    private boolean isMatch(String url, String pattern) {
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }

    /**
     * 获取令牌
     * @param request
     * @return
     */
    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(HttpConstants.AUTHENTICATION);
        //如果设置了令牌前缀，则裁剪掉前缀
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        return token;
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
