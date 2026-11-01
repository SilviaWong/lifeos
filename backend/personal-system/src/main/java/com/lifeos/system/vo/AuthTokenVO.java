package com.lifeos.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 认证 Token 响应视图
 */
@Schema(description = "认证授权令牌响应")
public class AuthTokenVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "JWT 访问令牌")
    private String token;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "有效时长 (秒)", example = "86400")
    private long expiresIn;

    @Schema(description = "当前用户信息")
    private UserVO userInfo;

    public AuthTokenVO() {
        this.tokenType = "Bearer";
    }

    public AuthTokenVO(String token, String tokenType, long expiresIn, UserVO userInfo) {
        this.token = token;
        this.tokenType = (tokenType != null && !tokenType.isBlank()) ? tokenType : "Bearer";
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserVO getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserVO userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public String toString() {
        return "AuthTokenVO{" +
                "token='***'" +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", userInfo=" + userInfo +
                '}';
    }
}

