package com.lifeos.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lifeos.common.api.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 番茄钟历史会话分页查询请求
 */
@Schema(description = "番茄钟历史会话分页查询请求")
public class PomodoroQueryRequest extends PageQuery {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话类型：FOCUS, SHORT_BREAK, LONG_BREAK", example = "FOCUS")
    private String sessionType;

    @Schema(description = "状态：RUNNING, PAUSED, COMPLETED, ABORTED", example = "COMPLETED")
    private String status;

    @Schema(description = "关联目标 ID", example = "101")
    private Long goalId;

    @Schema(description = "关联待办 ID", example = "201")
    private Long todoId;

    @Schema(description = "起始日期 (yyyy-MM-dd)", example = "2026-10-25")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "截止日期 (yyyy-MM-dd)", example = "2026-11-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    public PomodoroQueryRequest() {
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    public Long getTodoId() {
        return todoId;
    }

    public void setTodoId(Long todoId) {
        this.todoId = todoId;
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

