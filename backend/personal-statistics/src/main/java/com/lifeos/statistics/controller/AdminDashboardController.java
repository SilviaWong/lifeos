package com.lifeos.statistics.controller;

import com.lifeos.common.api.ApiResponse;
import com.lifeos.statistics.dto.AdminDashboardQueryRequest;
import com.lifeos.statistics.service.AdminDashboardService;
import com.lifeos.statistics.vo.AdminDashboardOverviewVO;
import com.lifeos.statistics.vo.AdminModuleTrendVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端综合数据看板接口控制器
 * 仅具有 ROLE_ADMIN 或 ROLE_SUPER_ADMIN 的管理员可访问（由 SecurityConfig 统一拦截）
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    /**
     * 获取管理端综合看板概览数据（指标卡片、双折线用户趋势、模块排行、健康分布、收支汇总）
     */
    @GetMapping("/overview")
    public ApiResponse<AdminDashboardOverviewVO> getDashboardOverview(AdminDashboardQueryRequest request) {
        return ApiResponse.success(adminDashboardService.getDashboardOverview(request));
    }

    /**
     * 获取各业务模块每日使用量趋势（用于堆叠面积/柱状趋势图）
     */
    @GetMapping("/module-trends")
    public ApiResponse<AdminModuleTrendVO> getModuleTrends(AdminDashboardQueryRequest request) {
        return ApiResponse.success(adminDashboardService.getModuleTrends(request));
    }
}
