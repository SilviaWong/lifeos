package com.lifeos.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 修改密码请求 DTO
 */
@Schema(description = "修改密码参数")
public class UpdatePasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "原密码", example = "old_password_123")
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码 (6-32位)", example = "new_password_123")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度必须在 6-32 位之间")
    private String newPassword;

    public UpdatePasswordDTO() {
    }

    public UpdatePasswordDTO(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public String toString() {
        return "UpdatePasswordDTO{oldPassword='***', newPassword='***'}";
    }
}

