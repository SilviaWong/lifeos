package com.lifeos.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lifeos.common.api.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 待办任务分页与筛选查询参数
 */
@Schema(description = "待办任务分页与筛选查询参数")
public class TodoQueryRequest extends PageQuery {

    private static final long serialVersionUID = 1L;

    @Schema(description = "按状态筛选：TODO, IN_PROGRESS, DONE, CANCELLED", example = "TODO")
    private String status;

    @Schema(description = "按优先级筛选：1-低, 2-中, 3-高, 4-紧急", example = "3")
    private Integer priority;

    @Schema(description = "按关键词搜索（标题或描述）", example = "阶段5")
    private String keyword;

    @Schema(description = "按关联目标筛选", example = "1001")
    private Long goalId;

    @Schema(description = "快捷筛选维度：ALL(全部), TODAY(今日), WEEK(本周), OVERDUE(已逾期), PENDING(待完成), COMPLETED(已完成)", example = "TODAY")
    private String filterType;

    @Schema(description = "开始日期 (yyyy-MM-dd)", example = "2026-11-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "截止日期 (yyyy-MM-dd)", example = "2026-11-07")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    public TodoQueryRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
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
}

