package com.lifeos.common.interceptor;

import com.lifeos.common.util.TraceIdUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求链路 TraceId 拦截器
 */
public class TraceIdInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientTraceId = request.getHeader(TraceIdUtil.HEADER_NAME);
        String traceId = TraceIdUtil.initTraceId(clientTraceId);
        response.setHeader(TraceIdUtil.HEADER_NAME, traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TraceIdUtil.clear();
    }
}

