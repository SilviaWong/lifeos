package com.lifeos.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 中止/放弃番茄钟专注请求参数
 */
@Schema(description = "中止番茄钟请求")
public class AbortPomodoroRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "中止原因说明", example = "USER_ABORT")
    @Size(max = 24, message = "原因长度不能超过 24 个字符")
    private String reason = "USER_ABORT";

    @Schema(description = "客户端上报的有效专注秒数", example = "600")
    private Integer clientActualSeconds;

    public AbortPomodoroRequest() {
    }

    public AbortPomodoroRequest(String reason, Integer clientActualSeconds) {
        this.reason = reason;
        this.clientActualSeconds = clientActualSeconds;
    }

    public String getReason() {
        return reason != null ? reason : "USER_ABORT";
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getClientActualSeconds() {
        return clientActualSeconds;
    }

    public void setClientActualSeconds(Integer clientActualSeconds) {
        this.clientActualSeconds = clientActualSeconds;
    }
}

