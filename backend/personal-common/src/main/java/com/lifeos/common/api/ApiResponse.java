package com.lifeos.common.api;

import com.lifeos.common.util.TraceIdUtil;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 全局统一接口响应结构
 *
 * @param <T> 数据载荷类型
 */
@Schema(description = "全局统一响应包装")
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态码：0 为成功，非 0 为异常或失败", example = "0")
    private int code;

    @Schema(description = "响应描述信息", example = "success")
    private String message;

    @Schema(description = "业务数据载荷")
    private T data;

    @Schema(description = "链路追踪ID", example = "e0b83647a5064db99480e66b4d32e92c")
    private String traceId;

    @Schema(description = "响应时间戳 (毫秒)", example = "1730419200000")
    private long timestamp;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
        this.traceId = TraceIdUtil.getTraceId();
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
        this.traceId = TraceIdUtil.getTraceId();
    }

    public ApiResponse(int code, String message, T data, String traceId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
        this.traceId = (traceId != null && !traceId.isBlank()) ? traceId : TraceIdUtil.getTraceId();
    }

    // 快捷静态构建方法

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(CommonErrorCode.SUCCESS.getCode(), CommonErrorCode.SUCCESS.getMessage(), null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(CommonErrorCode.SUCCESS.getCode(), CommonErrorCode.SUCCESS.getMessage(), data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(CommonErrorCode.SUCCESS.getCode(), message, data);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String customMessage) {
        return new ApiResponse<>(errorCode.getCode(), customMessage, null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    // Getters and Setters

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", traceId='" + traceId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

