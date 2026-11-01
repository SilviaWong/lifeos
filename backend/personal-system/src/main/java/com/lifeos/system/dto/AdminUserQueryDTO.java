package com.lifeos.system.dto;

import com.lifeos.common.api.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 管理端用户列表查询参数
 */
@Schema(description = "管理端用户列表查询条件")
public class AdminUserQueryDTO extends PageQuery {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名模糊搜索", example = "admin")
    private String username;

    @Schema(description = "手机号精准搜索", example = "13800000000")
    private String phone;

    @Schema(description = "账号状态：0 禁用，1 启用", example = "1")
    private Integer status;

    @Schema(description = "角色编码过滤：USER / ADMIN / SUPER_ADMIN", example = "USER")
    private String roleCode;

    public AdminUserQueryDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode != null ? roleCode.trim() : null;
    }
}

