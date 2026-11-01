package com.lifeos.business.controller;

import com.lifeos.business.dto.ReminderMessageQueryRequest;
import com.lifeos.business.service.ReminderMessageService;
import com.lifeos.business.service.ReminderSchedulerService;
import com.lifeos.business.vo.ReminderExecutionSummaryVO;
import com.lifeos.business.vo.ReminderMessageVO;
import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.PageResult;
import org.springframework.web.bind.annotation.*;

/**
 * 站内提醒消息接口控制器
 */
@RestController
@RequestMapping("/api/reminders/messages")
public class ReminderMessageController {

    private final ReminderMessageService reminderMessageService;
    private final ReminderSchedulerService reminderSchedulerService;

    public ReminderMessageController(ReminderMessageService reminderMessageService,
                                     ReminderSchedulerService reminderSchedulerService) {
        this.reminderMessageService = reminderMessageService;
        this.reminderSchedulerService = reminderSchedulerService;
    }

    /**
     * 分页查询消息列表
     */
    @GetMapping
    public ApiResponse<PageResult<ReminderMessageVO>> queryMessages(ReminderMessageQueryRequest request) {
        return ApiResponse.success(reminderMessageService.queryMessages(request));
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadCount() {
        return ApiResponse.success(reminderMessageService.countUnread());
    }

    /**
     * 获取提醒与消息概览统计
     */
    @GetMapping("/summary")
    public ApiResponse<ReminderExecutionSummaryVO> getSummary() {
        return ApiResponse.success(reminderMessageService.getSummary());
    }

    /**
     * 查看单条消息详情（自动标为已读）
     */
    @GetMapping("/{id}")
    public ApiResponse<ReminderMessageVO> getMessage(@PathVariable Long id) {
        return ApiResponse.success(reminderMessageService.getMessage(id));
    }

    /**
     * 单条标为已读
     */
    @PutMapping("/{id}/read")
    public ApiResponse<ReminderMessageVO> markAsRead(@PathVariable Long id) {
        return ApiResponse.success(reminderMessageService.markAsRead(id));
    }

    /**
     * 一键全部标为已读
     */
    @PutMapping("/read-all")
    public ApiResponse<Integer> markAllAsRead() {
        return ApiResponse.success(reminderMessageService.markAllAsRead());
    }

    /**
     * 删除单条消息
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMessage(@PathVariable Long id) {
        reminderMessageService.deleteMessage(id);
        return ApiResponse.success();
    }

    /**
     * 手动触发扫描提醒规则（用于即时生效或调度验证）
     */
    @PostMapping("/trigger-scan")
    public ApiResponse<Integer> triggerScan() {
        int count = reminderSchedulerService.scanAndTriggerReminders();
        return ApiResponse.success(count);
    }
}
