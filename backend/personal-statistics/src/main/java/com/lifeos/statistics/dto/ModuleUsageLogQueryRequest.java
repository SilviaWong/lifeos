package com.lifeos.statistics.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 模块操作审计使用日志分页查询请求
 */
public class ModuleUsageLogQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模块编码（如 TODO, HEALTH, GOAL, POMODORO, HABIT, FINANCE）
     */
    private String moduleCode;

    /**
     * 动作编码（如 CREATE, UPDATE, DELETE, COMPLETE, CHECKIN, FINISH 等）
     */
    private String actionCode;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 链路请求追踪 ID
     */
    private String requestId;

    /**
     * 开始日期 (yyyy-MM-dd)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期 (yyyy-MM-dd)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 当前页码，从 1 开始
     */
    private Integer current = 1;

    /**
     * 每页大小，默认 10，最大 100
     */
    private Integer size = 10;

    public ModuleUsageLogQueryRequest() {
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public Integer getCurrent() {
        return current != null && current > 0 ? current : 1;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getSize() {
        if (size == null || size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}

