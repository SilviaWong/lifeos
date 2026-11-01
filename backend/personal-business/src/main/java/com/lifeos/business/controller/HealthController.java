package com.lifeos.business.controller;

import com.lifeos.business.dto.CreateHealthRecordRequest;
import com.lifeos.business.dto.HealthRecordQueryRequest;
import com.lifeos.business.dto.HealthTrendQueryRequest;
import com.lifeos.business.dto.UpdateHealthRecordRequest;
import com.lifeos.business.service.HealthRecordService;
import com.lifeos.business.service.HealthStandardService;
import com.lifeos.business.vo.HealthDashboardSummaryVO;
import com.lifeos.business.vo.HealthRecordVO;
import com.lifeos.business.vo.HealthStandardVO;
import com.lifeos.business.vo.HealthTrendVO;
import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.PageResult;
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

import java.util.List;

/**
 * 个人健康指标记录与趋势控制器
 */
@Tag(name = "健康管理", description = "步数、睡眠、饮水、体重、饮食等健康数据录入、趋势分析与今日看板")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthRecordService healthRecordService;
    private final HealthStandardService healthStandardService;

    public HealthController(HealthRecordService healthRecordService,
                            HealthStandardService healthStandardService) {
        this.healthRecordService = healthRecordService;
        this.healthStandardService = healthStandardService;
    }

    @Operation(summary = "录入健康数据记录")
    @PostMapping("/records")
    public ApiResponse<HealthRecordVO> createRecord(@Valid @RequestBody CreateHealthRecordRequest request) {
        HealthRecordVO vo = healthRecordService.createRecord(request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "修改健康数据记录")
    @PutMapping("/records/{id}")
    public ApiResponse<HealthRecordVO> updateRecord(
            @Parameter(description = "记录 ID", required = true) @PathVariable("id") Long id,
            @Valid @RequestBody UpdateHealthRecordRequest request) {
        HealthRecordVO vo = healthRecordService.updateRecord(id, request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "删除健康数据记录")
    @DeleteMapping("/records/{id}")
    public ApiResponse<Void> deleteRecord(
            @Parameter(description = "记录 ID", required = true) @PathVariable("id") Long id) {
        healthRecordService.deleteRecord(id);
        return ApiResponse.success("记录删除成功", null);
    }

    @Operation(summary = "获取健康数据记录详情")
    @GetMapping("/records/{id}")
    public ApiResponse<HealthRecordVO> getRecordById(
            @Parameter(description = "记录 ID", required = true) @PathVariable("id") Long id) {
        HealthRecordVO vo = healthRecordService.getRecordById(id);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "分页查询健康数据记录")
    @GetMapping("/records")
    public ApiResponse<PageResult<HealthRecordVO>> queryRecords(@Valid HealthRecordQueryRequest request) {
        PageResult<HealthRecordVO> result = healthRecordService.queryRecords(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "查询健康指标多维趋势数据")
    @GetMapping("/trends")
    public ApiResponse<HealthTrendVO> getHealthTrend(@Valid HealthTrendQueryRequest request) {
        HealthTrendVO trend = healthRecordService.getHealthTrend(request);
        return ApiResponse.success(trend);
    }

    @Operation(summary = "获取用户今日健康看板快照汇总")
    @GetMapping("/summary")
    public ApiResponse<HealthDashboardSummaryVO> getDashboardSummary() {
        HealthDashboardSummaryVO summary = healthRecordService.getDashboardSummary();
        return ApiResponse.success(summary);
    }

    @Operation(summary = "获取生效中的健康参考标准列表（生活参考）")
    @GetMapping("/standards")
    public ApiResponse<List<HealthStandardVO>> getActiveStandards() {
        List<HealthStandardVO> list = healthStandardService.getActiveStandards();
        return ApiResponse.success(list);
    }
}

