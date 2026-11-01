package com.lifeos.statistics.controller;

import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.PageResult;
import com.lifeos.statistics.dto.AdminDashboardQueryRequest;
import com.lifeos.statistics.dto.ModuleUsageLogQueryRequest;
import com.lifeos.statistics.service.AdminOperationService;
import com.lifeos.statistics.vo.ModuleUsageLogVO;
import com.lifeos.statistics.vo.ModuleUsageRankVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端运营管理接口控制器（模块调用排行与模块操作审计日志查询）
 * 仅具有 ROLE_ADMIN 或 ROLE_SUPER_ADMIN 的管理员可访问（由 SecurityConfig 统一拦截）
 */
@RestController
@RequestMapping("/api/admin/operations")
public class AdminOperationController {

    private final AdminOperationService adminOperationService;

    public AdminOperationController(AdminOperationService adminOperationService) {
        this.adminOperationService = adminOperationService;
    }

    /**
     * 分页查询模块操作审计日志
     */
    @GetMapping("/usage-logs")
    public ApiResponse<PageResult<ModuleUsageLogVO>> queryUsageLogs(ModuleUsageLogQueryRequest request) {
        return ApiResponse.success(adminOperationService.queryUsageLogs(request));
    }

    /**
     * 查询模块使用量与独立用户数排行
     */
    @GetMapping("/module-ranking")
    public ApiResponse<List<ModuleUsageRankVO>> getModuleRanking(AdminDashboardQueryRequest request) {
        return ApiResponse.success(adminOperationService.getModuleUsageRanking(request));
    }
}
