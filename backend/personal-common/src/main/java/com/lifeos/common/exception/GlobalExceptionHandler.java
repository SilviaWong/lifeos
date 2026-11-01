package com.lifeos.common.exception;

import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.CommonErrorCode;
import com.lifeos.common.util.TraceIdUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局统一异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 自定义业务异常拦截
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("[BusinessException] code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    /**
     * DTO 参数校验异常拦截 (@Valid @RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String detailMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("[MethodArgumentNotValidException] {}", detailMessage);
        return ApiResponse.fail(CommonErrorCode.BAD_REQUEST, detailMessage);
    }

    /**
     * 表单绑定校验异常拦截
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBindException(BindException e) {
        String detailMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("[BindException] {}", detailMessage);
        return ApiResponse.fail(CommonErrorCode.BAD_REQUEST, detailMessage);
    }

    /**
     * URL/Query 约束校验异常 (@Validated @RequestParam/@PathVariable)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String detailMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("[ConstraintViolationException] {}", detailMessage);
        return ApiResponse.fail(CommonErrorCode.BAD_REQUEST, detailMessage);
    }

    /**
     * 缺少必要请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String message = "缺少必要请求参数: " + e.getParameterName();
        log.warn("[MissingServletRequestParameterException] {}", message);
        return ApiResponse.fail(CommonErrorCode.BAD_REQUEST, message);
    }

    /**
     * 参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("参数 [%s] 类型错误，应为 %s", e.getName(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        log.warn("[MethodArgumentTypeMismatchException] {}", message);
        return ApiResponse.fail(CommonErrorCode.BAD_REQUEST, message);
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String message = "当前接口不支持 " + e.getMethod() + " 请求方法";
        log.warn("[HttpRequestMethodNotSupportedException] {}", message);
        return ApiResponse.fail(CommonErrorCode.METHOD_NOT_ALLOWED, message);
    }

    /**
     * 静态资源或路径未找到 (Spring Boot 3)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("[NoResourceFoundException] resourcePath={}", e.getResourcePath());
        return ApiResponse.fail(CommonErrorCode.NOT_FOUND, "请求的资源或路径不存在: " + e.getResourcePath());
    }

    /**
     * 未知兜底异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGenericException(Exception e) {
        log.error("[SystemUnhandledException] traceId={}, ex={}", TraceIdUtil.getTraceId(), e.getMessage(), e);
        return ApiResponse.fail(CommonErrorCode.INTERNAL_SERVER_ERROR, "系统内部繁忙，请稍后再试");
    }
}

