package com.lifeos.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 用户注册请求 DTO
 */
@Schema(description = "用户注册参数")
public class RegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名 (3-32位字母数字下划线)", example = "developer01")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度必须在 3-32 位之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含英文字母、数字和下划线")
    private String username;

    @Schema(description = "密码 (6-32位)", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在 6-32 位之间")
    private String password;

    @Schema(description = "用户昵称", example = "星辰")
    @Size(max = 64, message = "昵称长度不能超过 64 个字符")
    private String nickname;

    @Schema(description = "手机号码", example = "13800000000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号码格式不合法")
    private String phone;

    @Schema(description = "电子邮箱", example = "dev@example.com")
    @Pattern(regexp = "^$|^[\\w.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$", message = "电子邮箱格式不合法")
    private String email;

    @Schema(description = "性别：0 未知，1 男，2 女", example = "1")
    private Integer gender;

    public RegisterDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : null;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname != null ? nickname.trim() : null;
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
        return "RegisterDTO{" +
                "username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", gender=" + gender +
                '}';
    }
}

