package com.lifeos.common.exception;

import com.lifeos.common.api.CommonErrorCode;
import com.lifeos.common.api.ErrorCode;

/**
 * 业务受检/非受检通用异常
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;
    private final ErrorCode errorCode;

    public BusinessException(String message) {
        super(message);
        this.code = CommonErrorCode.BUSINESS_ERROR.getCode();
        this.errorCode = CommonErrorCode.BUSINESS_ERROR;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.errorCode = CommonErrorCode.BUSINESS_ERROR;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }

    public int getCode() {
        return code;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

