package com.lifeos.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 目标状态流转请求参数
 */
@Schema(description = "目标状态流转请求参数")
public class UpdateGoalStatusRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "目标状态：NOT_STARTED, IN_PROGRESS, COMPLETED, ARCHIVED", requiredMode = Schema.RequiredMode.REQUIRED, example = "COMPLETED")
    @NotBlank(message = "目标状态不能为空")
    private String status;

    public UpdateGoalStatusRequest() {
    }

    public UpdateGoalStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

