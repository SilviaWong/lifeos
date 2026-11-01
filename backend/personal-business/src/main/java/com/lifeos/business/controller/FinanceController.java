package com.lifeos.business.controller;

import com.lifeos.business.dto.*;
import com.lifeos.business.service.FinanceCategoryService;
import com.lifeos.business.service.FinanceRecordService;
import com.lifeos.business.vo.*;
import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.PageResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 个人收支记录与财务统计控制器
 */
@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinanceRecordService financeRecordService;
    private final FinanceCategoryService financeCategoryService;

    public FinanceController(FinanceRecordService financeRecordService,
                             FinanceCategoryService financeCategoryService) {
        this.financeRecordService = financeRecordService;
        this.financeCategoryService = financeCategoryService;
    }

    /**
     * 新增收支明细
     */
    @PostMapping("/records")
    public ApiResponse<FinanceRecordVO> createRecord(@RequestBody @Valid CreateFinanceRecordRequest request) {
        return ApiResponse.success(financeRecordService.createRecord(request));
    }

    /**
     * 修改收支明细
     */
    @PutMapping("/records/{id}")
    public ApiResponse<FinanceRecordVO> updateRecord(@PathVariable Long id,
                                                     @RequestBody @Valid UpdateFinanceRecordRequest request) {
        return ApiResponse.success(financeRecordService.updateRecord(id, request));
    }

    /**
     * 删除收支明细
     */
    @DeleteMapping("/records/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        financeRecordService.deleteRecord(id);
        return ApiResponse.success();
    }

    /**
     * 获取单笔收支明细详情
     */
    @GetMapping("/records/{id}")
    public ApiResponse<FinanceRecordVO> getRecordById(@PathVariable Long id) {
        return ApiResponse.success(financeRecordService.getRecordById(id));
    }

    /**
     * 分页多条件查询收支明细
     */
    @GetMapping("/records")
    public ApiResponse<PageResult<FinanceRecordVO>> queryRecords(FinanceRecordQueryRequest request) {
        return ApiResponse.success(financeRecordService.queryRecords(request));
    }

    /**
     * 月度收支汇总（收入合计、支出合计、结余、分类占比）
     */
    @GetMapping("/summary")
    public ApiResponse<FinanceSummaryVO> getMonthlySummary(@RequestParam(required = false) Integer year,
                                                          @RequestParam(required = false) Integer month) {
        return ApiResponse.success(financeRecordService.getMonthlySummary(year, month));
    }

    /**
     * 查询收支月度趋势
     */
    @GetMapping("/trends")
    public ApiResponse<FinanceTrendVO> getTrends(@RequestParam(required = false) Integer year,
                                                 @RequestParam(defaultValue = "6") Integer months) {
        return ApiResponse.success(financeRecordService.getTrends(year, months));
    }

    /**
     * 查询分类统计明细
     */
    @GetMapping("/category-stats")
    public ApiResponse<List<FinanceCategoryStatVO>> getCategoryStats(@RequestParam(required = false) Integer year,
                                                                    @RequestParam(required = false) Integer month,
                                                                    @RequestParam(required = false) String recordType) {
        return ApiResponse.success(financeRecordService.getCategoryStats(year, month, recordType));
    }

    /**
     * 查询当前用户可用的收支分类列表
     */
    @GetMapping("/categories")
    public ApiResponse<List<FinanceCategoryVO>> getCategories(@RequestParam(required = false) String categoryType) {
        return ApiResponse.success(financeCategoryService.getCategoriesForUser(categoryType));
    }

    /**
     * 创建用户自定义分类
     */
    @PostMapping("/categories")
    public ApiResponse<FinanceCategoryVO> createCustomCategory(@RequestBody @Valid CreateFinanceCategoryRequest request) {
        return ApiResponse.success(financeCategoryService.createCustomCategory(request));
    }

    /**
     * 修改用户自定义分类
     */
    @PutMapping("/categories/{id}")
    public ApiResponse<FinanceCategoryVO> updateCustomCategory(@PathVariable Long id,
                                                               @RequestBody @Valid UpdateFinanceCategoryRequest request) {
        return ApiResponse.success(financeCategoryService.updateCustomCategory(id, request));
    }

    /**
     * 删除用户自定义分类
     */
    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCustomCategory(@PathVariable Long id) {
        financeCategoryService.deleteCustomCategory(id);
        return ApiResponse.success();
    }
}

