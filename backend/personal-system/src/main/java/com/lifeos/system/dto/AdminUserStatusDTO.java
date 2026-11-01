package com.lifeos.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 管理端更新用户状态请求 DTO
 */
@Schema(description = "管理端修改用户状态参数")
public class AdminUserStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态：0 禁用，1 启用", example = "0")
    @NotNull(message = "状态值不能为空")
    @Min(value = 0, message = "状态值只能为 0 或 1")
    @Max(value = 1, message = "状态值只能为 0 或 1")
    private Integer status;

    public AdminUserStatusDTO() {
    }

    public AdminUserStatusDTO(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

