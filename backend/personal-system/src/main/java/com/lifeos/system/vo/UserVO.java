package com.lifeos.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 用户信息视图对象 (对外安全契约，绝不包含密码哈希)
 */
@Schema(description = "用户信息响应视图")
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "developer01")
    private String username;

    @Schema(description = "昵称", example = "星辰")
    private String nickname;

    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    @Schema(description = "电子邮箱", example = "dev@example.com")
    private String email;

    @Schema(description = "头像URL", example = "https://example.com/avatar.png")
    private String avatarUrl;

    @Schema(description = "性别：0 未知，1 男，2 女", example = "1")
    private Integer gender;

    @Schema(description = "账号状态：0 禁用，1 启用", example = "1")
    private Integer status;

    @Schema(description = "角色列表", example = "[\"USER\"]")
    private List<String> roles;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "注册创建时间")
    private LocalDateTime createdAt;

    public UserVO() {
        this.roles = Collections.emptyList();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles != null ? roles : Collections.emptyList();
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

