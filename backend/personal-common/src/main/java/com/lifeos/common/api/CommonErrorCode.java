package com.lifeos.common.api;

/**
 * 通用业务错误码枚举
 */
public enum CommonErrorCode implements ErrorCode {

    SUCCESS(0, "操作成功"),
    BAD_REQUEST(400, "请求参数校验不通过"),
    UNAUTHORIZED(401, "请先登录或身份认证已失效"),
    FORBIDDEN(403, "当前账号无权限执行此操作"),
    NOT_FOUND(404, "请求的资源或接口不存在"),
    METHOD_NOT_ALLOWED(405, "不支持当前 HTTP 请求方法"),
    INTERNAL_SERVER_ERROR(500, "系统内部繁忙，请稍后再试"),

    // 业务通用错误码 (1000 - 1999)
    BUSINESS_ERROR(1001, "业务处理失败"),
    OPERATION_FREQUENT(1002, "操作过于频繁，请稍后再试"),
    DATA_INTEGRITY_VIOLATION(1003, "数据完整性约束校验失败"),
    RESOURCE_ALREADY_EXISTS(1004, "目标资源已存在"),
    RESOURCE_NOT_FOUND(1005, "目标资源未找到");

    private final int code;
    private final String message;

    CommonErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

