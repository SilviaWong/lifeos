package com.lifeos.system.controller;

import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.PageResult;
import com.lifeos.system.dto.AdminUserQueryDTO;
import com.lifeos.system.dto.AdminUserStatusDTO;
import com.lifeos.system.service.AdminUserService;
import com.lifeos.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端用户运营管理控制器 (受 ADMIN / SUPER_ADMIN 权限保护)
 */
@Tag(name = "管理端-用户管理", description = "平台用户列表检索、状态启用与禁用")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(summary = "分页查询平台用户列表")
    @GetMapping
    public ApiResponse<PageResult<UserVO>> getUserPage(@Valid AdminUserQueryDTO queryDTO) {
        PageResult<UserVO> pageResult = adminUserService.getUserPage(queryDTO);
        return ApiResponse.success(pageResult);
    }

    @Operation(summary = "更新目标用户账号状态 (启用/禁用)")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateUserStatus(@PathVariable("id") Long id,
                                             @Valid @RequestBody AdminUserStatusDTO statusDTO) {
        adminUserService.updateUserStatus(id, statusDTO.getStatus());
        return ApiResponse.success("用户状态修改成功", null);
    }
}

