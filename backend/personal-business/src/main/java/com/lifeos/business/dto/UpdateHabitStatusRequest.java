package com.lifeos.business.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 更新习惯状态请求参数
 */
public class UpdateHabitStatusRequest {

    @NotBlank(message = "状态不能为空")
    private String status;

    public UpdateHabitStatusRequest() {
    }

    public UpdateHabitStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

