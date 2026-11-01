package com.lifeos.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 目标子任务状态更新请求参数
 */
@Schema(description = "目标子任务状态更新请求参数")
public class UpdateGoalTaskStatusRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "子任务状态：TODO, DONE", requiredMode = Schema.RequiredMode.REQUIRED, example = "DONE")
    @NotBlank(message = "状态不能为空")
    private String status;

    public UpdateGoalTaskStatusRequest() {
    }

    public UpdateGoalTaskStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

