package com.lifeos.business.controller;

import com.lifeos.business.dto.CreateFinanceCategoryRequest;
import com.lifeos.business.dto.UpdateFinanceCategoryRequest;
import com.lifeos.business.service.FinanceCategoryService;
import com.lifeos.business.vo.FinanceCategoryVO;
import com.lifeos.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端收支系统分类管理控制器 (受 ADMIN / SUPER_ADMIN 权限保护)
 */
@Tag(name = "管理端-收支分类维护", description = "系统预设收支分类 CRUD")
@RestController
@RequestMapping("/api/admin/finance-categories")
public class AdminFinanceCategoryController {

    private final FinanceCategoryService financeCategoryService;

    public AdminFinanceCategoryController(FinanceCategoryService financeCategoryService) {
        this.financeCategoryService = financeCategoryService;
    }

    @Operation(summary = "查询所有系统分类列表")
    @GetMapping
    public ApiResponse<List<FinanceCategoryVO>> listSystemCategories(@RequestParam(required = false) String categoryType) {
        return ApiResponse.success(financeCategoryService.listAllSystemCategories(categoryType));
    }

    @Operation(summary = "创建系统预设分类")
    @PostMapping
    public ApiResponse<FinanceCategoryVO> createSystemCategory(@Valid @RequestBody CreateFinanceCategoryRequest request) {
        return ApiResponse.success(financeCategoryService.createSystemCategory(request));
    }

    @Operation(summary = "修改系统预设分类")
    @PutMapping("/{id}")
    public ApiResponse<FinanceCategoryVO> updateSystemCategory(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateFinanceCategoryRequest request) {
        return ApiResponse.success(financeCategoryService.updateSystemCategory(id, request));
    }

    @Operation(summary = "删除系统预设分类")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSystemCategory(@PathVariable Long id) {
        financeCategoryService.deleteSystemCategory(id);
        return ApiResponse.success();
    }
}

