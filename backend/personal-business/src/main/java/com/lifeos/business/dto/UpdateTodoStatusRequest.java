package com.lifeos.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 修改待办任务状态请求参数
 */
@Schema(description = "修改待办任务状态请求参数")
public class UpdateTodoStatusRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "新状态：TODO, IN_PROGRESS, DONE, CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "DONE")
    @NotBlank(message = "任务状态不能为空")
    private String status;

    public UpdateTodoStatusRequest() {
    }

    public UpdateTodoStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

