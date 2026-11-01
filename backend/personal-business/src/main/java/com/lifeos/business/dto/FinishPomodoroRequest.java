package com.lifeos.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 完成番茄钟专注请求参数
 */
@Schema(description = "完成番茄钟请求")
public class FinishPomodoroRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "客户端上报的有效专注秒数（服务端会做时间合理性比对和防护）", example = "1500")
    private Integer clientActualSeconds;

    public FinishPomodoroRequest() {
    }

    public FinishPomodoroRequest(Integer clientActualSeconds) {
        this.clientActualSeconds = clientActualSeconds;
    }

    public Integer getClientActualSeconds() {
        return clientActualSeconds;
    }

    public void setClientActualSeconds(Integer clientActualSeconds) {
        this.clientActualSeconds = clientActualSeconds;
    }
}

