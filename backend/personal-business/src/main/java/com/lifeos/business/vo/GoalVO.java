package com.lifeos.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 目标详情视图对象
 */
@Schema(description = "目标详情视图对象")
public class GoalVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "目标 ID", example = "1")
    private Long id;

    @Schema(description = "所属用户 ID", example = "10")
    private Long userId;

    @Schema(description = "目标标题", example = "通过系统架构设计师认证")
    private String title;

    @Schema(description = "目标描述", example = "包含论文写作与案例分析备考")
    private String description;

    @Schema(description = "开始日期", example = "2026-11-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "截止日期", example = "2026-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @Schema(description = "优先级：1-高, 2-中, 3-低", example = "1")
    private Integer priority;

    @Schema(description = "优先级中文说明", example = "高")
    private String priorityDesc;

    @Schema(description = "目标状态：NOT_STARTED, IN_PROGRESS, COMPLETED, ARCHIVED", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "目标状态中文说明", example = "进行中")
    private String statusDesc;

    @Schema(description = "服务端计算的目标进度百分比 (0.00 ~ 100.00)", example = "50.00")
    private BigDecimal progress;

    @Schema(description = "进度文本表达，如「尚未拆解」或「50%」", example = "50%")
    private String progressText;

    @Schema(description = "有效子任务总数", example = "4")
    private int totalTaskCount;

    @Schema(description = "已完成子任务数", example = "2")
    private int completedTaskCount;

    @Schema(description = "完成时间", example = "2026-12-25 18:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    @Schema(description = "子任务明细列表（详情接口返回）")
    private List<GoalTaskVO> tasks;

    @Schema(description = "创建时间", example = "2026-11-01 09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-11-01 09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public GoalVO() {
        this.tasks = Collections.emptyList();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getPriorityDesc() {
        return priorityDesc;
    }

    public void setPriorityDesc(String priorityDesc) {
        this.priorityDesc = priorityDesc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public BigDecimal getProgress() {
        return progress;
    }

    public void setProgress(BigDecimal progress) {
        this.progress = progress;
    }

    public String getProgressText() {
        return progressText;
    }

    public void setProgressText(String progressText) {
        this.progressText = progressText;
    }

    public int getTotalTaskCount() {
        return totalTaskCount;
    }

    public void setTotalTaskCount(int totalTaskCount) {
        this.totalTaskCount = totalTaskCount;
    }

    public int getCompletedTaskCount() {
        return completedTaskCount;
    }

    public void setCompletedTaskCount(int completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public List<GoalTaskVO> getTasks() {
        return tasks;
    }

    public void setTasks(List<GoalTaskVO> tasks) {
        this.tasks = tasks != null ? tasks : Collections.emptyList();
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

