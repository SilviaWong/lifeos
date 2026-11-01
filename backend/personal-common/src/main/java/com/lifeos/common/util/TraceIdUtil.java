package com.lifeos.common.util;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * 请求链路追踪标识工具类
 */
public final class TraceIdUtil {

    public static final String MDC_KEY = "traceId";
    public static final String HEADER_NAME = "X-Trace-Id";

    private TraceIdUtil() {
    }

    /**
     * 生成并注入新的 TraceId 到当前线程上下文
     */
    public static String initTraceId(String existingTraceId) {
        String traceId = (existingTraceId != null && !existingTraceId.isBlank())
                ? existingTraceId
                : UUID.randomUUID().toString().replace("-", "");
        MDC.put(MDC_KEY, traceId);
        return traceId;
    }

    /**
     * 获取当前线程上下文中的 TraceId
     */
    public static String getTraceId() {
        String traceId = MDC.get(MDC_KEY);
        return traceId != null ? traceId : "";
    }

    /**
     * 清理当前线程上下文中的 TraceId
     */
    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}

