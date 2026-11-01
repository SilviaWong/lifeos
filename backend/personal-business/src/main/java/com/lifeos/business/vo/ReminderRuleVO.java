package com.lifeos.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 提醒规则视图对象
 */
public class ReminderRuleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String ruleName;

    private String triggerType;

    private String triggerTypeDesc;

    private Long businessRefId;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime remindAt;

    private Integer advanceMinutes;

    private String repeatRule;

    private String repeatRuleDesc;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Integer status;

    private String statusDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public ReminderRuleVO() {
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

    public String getTriggerTypeDesc() {
        return triggerTypeDesc;
    }

    public void setTriggerTypeDesc(String triggerTypeDesc) {
        this.triggerTypeDesc = triggerTypeDesc;
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

    public String getRepeatRuleDesc() {
        return repeatRuleDesc;
    }

    public void setRepeatRuleDesc(String repeatRuleDesc) {
        this.repeatRuleDesc = repeatRuleDesc;
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

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
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
}

