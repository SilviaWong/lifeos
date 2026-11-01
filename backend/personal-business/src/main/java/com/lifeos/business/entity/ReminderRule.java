package com.lifeos.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 提醒规则实体类
 */
@TableName("reminder_rule")
public class ReminderRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 规则名称，例如：每日晨间打卡提醒、待办截止前提醒
     */
    private String ruleName;

    /**
     * 触发类型：TODO_DUE, HABIT_CHECKIN, GOAL_DUE, CUSTOM
     */
    private String triggerType;

    /**
     * 关联业务ID（如具体的 todo_id, habit_id, goal_id），为空表示适用同类型所有对象
     */
    private Long businessRefId;

    /**
     * 每日提醒时间（例如 08:30:00）
     */
    private LocalTime remindAt;

    /**
     * 提前提醒分钟数（例如 15 分钟）
     */
    private Integer advanceMinutes;

    /**
     * 重复规则：DAILY（每天）, WORKDAY（工作日）, WEEKEND（周末）, ONCE（单次）
     */
    private String repeatRule;

    /**
     * 生效开始日期
     */
    private LocalDate startDate;

    /**
     * 生效截止日期
     */
    private LocalDate endDate;

    /**
     * 规则状态：1-启用, 0-停用
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    public ReminderRule() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Long getBusinessRefId() {
        return businessRefId;
    }

    public void setBusinessRefId(Long businessRefId) {
        this.businessRefId = businessRefId;
    }

    public LocalTime getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(LocalTime remindAt) {
        this.remindAt = remindAt;
    }

    public Integer getAdvanceMinutes() {
        return advanceMinutes;
    }

    public void setAdvanceMinutes(Integer advanceMinutes) {
        this.advanceMinutes = advanceMinutes;
    }

    public String getRepeatRule() {
        return repeatRule;
    }

    public void setRepeatRule(String repeatRule) {
        this.repeatRule = repeatRule;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}

