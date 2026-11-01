package com.lifeos.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 更新提醒规则请求 DTO
 */
public class UpdateReminderRuleRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称最多 100 个字符")
    private String ruleName;

    @NotBlank(message = "触发类型不能为空")
    @Pattern(regexp = "^(TODO_DUE|HABIT_CHECKIN|GOAL_DUE|CUSTOM)$", message = "触发类型必须为 TODO_DUE / HABIT_CHECKIN / GOAL_DUE / CUSTOM")
    private String triggerType;

    private Long businessRefId;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime remindAt;

    private Integer advanceMinutes = 0;

    private String repeatRule = "DAILY";

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Integer status = 1;

    public UpdateReminderRuleRequest() {
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
}

