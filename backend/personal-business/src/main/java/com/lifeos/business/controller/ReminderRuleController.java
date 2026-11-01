package com.lifeos.business.controller;

import com.lifeos.business.dto.CreateReminderRuleRequest;
import com.lifeos.business.dto.ReminderRuleQueryRequest;
import com.lifeos.business.dto.UpdateReminderRuleRequest;
import com.lifeos.business.service.ReminderRuleService;
import com.lifeos.business.vo.ReminderRuleVO;
import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.PageResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 提醒规则接口控制器
 */
@RestController
@RequestMapping("/api/reminders/rules")
public class ReminderRuleController {

    private final ReminderRuleService reminderRuleService;

    public ReminderRuleController(ReminderRuleService reminderRuleService) {
        this.reminderRuleService = reminderRuleService;
    }

    /**
     * 新增提醒规则
     */
    @PostMapping
    public ApiResponse<ReminderRuleVO> createRule(@Valid @RequestBody CreateReminderRuleRequest request) {
        return ApiResponse.success(reminderRuleService.createRule(request));
    }

    /**
     * 更新提醒规则
     */
    @PutMapping("/{id}")
    public ApiResponse<ReminderRuleVO> updateRule(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateReminderRuleRequest request) {
        return ApiResponse.success(reminderRuleService.updateRule(id, request));
    }

    /**
     * 删除提醒规则
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        reminderRuleService.deleteRule(id);
        return ApiResponse.success();
    }

    /**
     * 启用 / 停用切换
     */
    @PutMapping("/{id}/status")
    public ApiResponse<ReminderRuleVO> toggleRuleStatus(@PathVariable Long id) {
        return ApiResponse.success(reminderRuleService.toggleRuleStatus(id));
    }

    /**
     * 获取规则详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ReminderRuleVO> getRule(@PathVariable Long id) {
        return ApiResponse.success(reminderRuleService.getRule(id));
    }

    /**
     * 分页查询提醒规则列表
     */
    @GetMapping
    public ApiResponse<PageResult<ReminderRuleVO>> queryRules(ReminderRuleQueryRequest request) {
        return ApiResponse.success(reminderRuleService.queryRules(request));
    }
}
