package com.lifeos.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 修改个人资料请求 DTO
 */
@Schema(description = "个人资料修改参数")
public class UpdateProfileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户昵称", example = "星辰大海")
    @Size(max = 64, message = "昵称长度不能超过 64 个字符")
    private String nickname;

    @Schema(description = "头像图片地址", example = "https://example.com/avatar.png")
    @Size(max = 500, message = "头像地址长度不能超过 500 个字符")
    private String avatarUrl;

    @Schema(description = "手机号码", example = "13912345678")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号码格式不合法")
    private String phone;

    @Schema(description = "电子邮箱", example = "user@example.com")
    @Pattern(regexp = "^$|^[\\w.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$", message = "电子邮箱格式不合法")
    private String email;

    @Schema(description = "性别：0 未知，1 男，2 女", example = "1")
    private Integer gender;

    public UpdateProfileDTO() {
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname != null ? nickname.trim() : null;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl != null ? avatarUrl.trim() : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "UpdateProfileDTO{" +
                "nickname='" + nickname + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", gender=" + gender +
                '}';
    }
}

