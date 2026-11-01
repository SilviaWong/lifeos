package com.lifeos.statistics.controller;

import com.lifeos.common.api.ApiResponse;
import com.lifeos.statistics.service.HomeSummaryService;
import com.lifeos.statistics.vo.HomeSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端首页综合聚合控制器
 */
@Tag(name = "首页综合看板", description = "用户端首页概要聚合、关键统计指标接口")
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeSummaryService homeSummaryService;

    public HomeController(HomeSummaryService homeSummaryService) {
        this.homeSummaryService = homeSummaryService;
    }

    @Operation(summary = "获取用户端首页聚合摘要")
    @GetMapping("/summary")
    public ApiResponse<HomeSummaryVO> getHomeSummary() {
        HomeSummaryVO summary = homeSummaryService.getHomeSummary();
        return ApiResponse.success(summary);
    }
}

