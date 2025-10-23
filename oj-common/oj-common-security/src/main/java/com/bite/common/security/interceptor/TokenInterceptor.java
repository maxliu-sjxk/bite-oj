package com.bite.common.security.interceptor;

import cn.hutool.core.util.StrUtil;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.constants.HttpConstants;
import com.bite.common.core.utils.ThreadLocalUtils;
import com.bite.common.security.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {


    @Autowired
    private TokenService tokenService;

    @Value("${jwt.secret}")
    private String secret;


    /**
     * 负责调用 token 延长方法
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取到token
        String token = getToken(request);

        if (StrUtil.isEmpty(token)) {
            return true;
        }
        Claims claims = tokenService.getClaims(token, secret);
        Long userId = tokenService.getUserId(claims);
        ThreadLocalUtils.set(Constants.USER_ID, userId);
        //调用延长方法
        tokenService.extendExpire(claims);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtils.remove();
    }

    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(HttpConstants.AUTHENTICATION);
        //如果设置了令牌前缀，则裁剪掉前缀
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        return token;
    }
}
