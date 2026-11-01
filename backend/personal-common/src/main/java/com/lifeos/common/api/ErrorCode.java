package com.lifeos.common.api;

/**
 * 统一错误码契约接口
 */
public interface ErrorCode {

    /**
     * 获取业务状态码（0 代表成功，非 0 代表异常或错误）
     */
    int getCode();

    /**
     * 获取错误提示描述
     */
    String getMessage();
}

