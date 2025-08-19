package com.bite.common.security.handler;

import com.bite.common.core.domain.R;
import com.bite.common.core.enums.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 请求方法不支持
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e,
                                                             HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}', 不支持'{}'请求", requestURI, e.getMethod());
        return R.fail(ResultCode.ERROR);
    }


    /**
     * 运行时异常
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public R<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}', 发生异常.", requestURI, e);
        return R.fail(ResultCode.ERROR);
    }

    /**
     * 全局异常兜底
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}', 发生异常.", requestURI, e);
        return R.fail(ResultCode.ERROR);
    }
}
