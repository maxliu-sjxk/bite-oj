package com.bite.common.security.interceptor;

import cn.hutool.core.util.StrUtil;
import com.bite.common.core.constants.HttpConstants;
import com.bite.common.security.service.TokenService;
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
        //调用延长方法
        tokenService.extendExpire(token, secret);
        return true;
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
