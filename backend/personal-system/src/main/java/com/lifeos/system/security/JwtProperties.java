package com.lifeos.system.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置属性绑定
 */
@Configuration
@ConfigurationProperties(prefix = "lifeos.jwt")
public class JwtProperties {

    /**
     * 签名密钥（生产环境必须通过环境变量注入至少 256 位安全密钥）
     */
    private String secret = "lifeos-jwt-secret-key-must-be-at-least-256-bits-long-2026-lifeos-system-key";

    /**
     * 令牌有效时长（秒），默认 24 小时 (86400 秒)
     */
    private long expiration = 86400L;

    /**
     * Token 请求头前缀，默认 "Bearer "
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Token 携带请求头名称，默认 "Authorization"
     */
    private String header = "Authorization";

    public JwtProperties() {
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}

