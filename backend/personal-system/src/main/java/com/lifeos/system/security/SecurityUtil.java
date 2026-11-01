package com.lifeos.system.security;

import com.lifeos.common.api.CommonErrorCode;
import com.lifeos.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

/**
 * 安全上下文辅助工具类
 * <p>
 * 严格保障用户 ID 仅从服务端已鉴权的 SecurityContext 中提取，不信任任何客户端传参
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * 获取当前认证成功的 SecurityUser 对象
     *
     * @return 当前登录用户主体
     * @throws BusinessException 未登录或身份无效时抛出 401 业务异常
     */
    public static SecurityUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser securityUser) {
            return securityUser;
        }

        throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }

    /**
     * 从服务端安全上下文中提取当前登录用户的 User ID
     *
     * @return 当前登录用户 ID
     */
    public static Long getLoginUserId() {
        return getCurrentUser().getUserId();
    }

    /**
     * 获取当前登录用户名
     */
    public static String getLoginUsername() {
        return getCurrentUser().getUsername();
    }

    /**
     * 获取当前登录用户的角色列表
     */
    public static List<String> getLoginRoles() {
        return getCurrentUser().getRoles();
    }

    /**
     * 判断当前登录用户是否拥有指定角色 (如 USER / ADMIN / SUPER_ADMIN)
     */
    public static boolean hasRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String targetAuthority = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (targetAuthority.equalsIgnoreCase(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断当前登录用户是否为管理员 (ADMIN 或 SUPER_ADMIN)
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("SUPER_ADMIN");
    }
}

