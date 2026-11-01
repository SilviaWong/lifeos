package com.lifeos.admin.controller;

import com.lifeos.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 应用健康检查接口
 */
@Tag(name = "系统探活", description = "应用启动状态与环境健康检查接口")
@RestController
@RequestMapping("/api")
public class HealthCheckController {

    @Value("${spring.application.name:lifeos-backend}")
    private String applicationName;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Operation(summary = "服务健康检查", description = "检查服务是否正常运行，返回系统名称、环境与当前时间")
    @GetMapping("/health-check")
    public ApiResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("application", applicationName);
        data.put("profile", activeProfile);
        data.put("serverTime", LocalDateTime.now());
        data.put("version", "1.0.0");
        return ApiResponse.success("服务运行正常", data);
    }
}

