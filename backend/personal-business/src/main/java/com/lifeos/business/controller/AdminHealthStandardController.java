package com.lifeos.business.controller;

import com.lifeos.business.dto.HealthStandardDTO;
import com.lifeos.business.service.HealthStandardService;
import com.lifeos.business.vo.HealthStandardVO;
import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.PageQuery;
import com.lifeos.common.api.PageResult;
import com.lifeos.system.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端健康指标参考标准配置控制器 (受 ADMIN / SUPER_ADMIN 保护)
 */
@Tag(name = "管理端-健康标准配置", description = "健康指标参考区间、生活指引与非医疗免责提示管理")
@RestController
@RequestMapping("/api/admin/health-standards")
public class AdminHealthStandardController {

    private final HealthStandardService healthStandardService;

    public AdminHealthStandardController(HealthStandardService healthStandardService) {
        this.healthStandardService = healthStandardService;
    }

    @Operation(summary = "创建健康指标参考标准")
    @PostMapping
    public ApiResponse<HealthStandardVO> createStandard(@Valid @RequestBody HealthStandardDTO dto) {
        Long adminUserId = SecurityUtil.getLoginUserId();
        HealthStandardVO vo = healthStandardService.createStandard(dto, adminUserId);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "修改健康指标参考标准")
    @PutMapping("/{id}")
    public ApiResponse<HealthStandardVO> updateStandard(
            @Parameter(description = "标准 ID", required = true) @PathVariable("id") Long id,
            @Valid @RequestBody HealthStandardDTO dto) {
        HealthStandardVO vo = healthStandardService.updateStandard(id, dto);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "删除健康指标参考标准")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStandard(
            @Parameter(description = "标准 ID", required = true) @PathVariable("id") Long id) {
        healthStandardService.deleteStandard(id);
        return ApiResponse.success("参考标准删除成功", null);
    }

    @Operation(summary = "分页查询健康指标参考标准")
    @GetMapping
    public ApiResponse<PageResult<HealthStandardVO>> queryStandards(@Valid PageQuery query) {
        PageResult<HealthStandardVO> result = healthStandardService.queryStandards(query);
        return ApiResponse.success(result);
    }
}

