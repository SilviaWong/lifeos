package com.lifeos.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 目标统计概要视图对象
 */
@Schema(description = "目标统计概要")
public class GoalSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "全部目标总数", example = "8")
    private long totalCount;

    @Schema(description = "进行中/未开始活跃目标数", example = "5")
    private long activeCount;

    @Schema(description = "已完成目标数", example = "2")
    private long completedCount;

    @Schema(description = "已归档目标数", example = "1")
    private long archivedCount;

    @Schema(description = "活跃目标平均进度", example = "42.50")
    private BigDecimal avgProgress;

    public GoalSummaryVO() {
        this.avgProgress = BigDecimal.ZERO;
    }

    public GoalSummaryVO(long totalCount, long activeCount, long completedCount, long archivedCount, BigDecimal avgProgress) {
        this.totalCount = totalCount;
        this.activeCount = activeCount;
        this.completedCount = completedCount;
        this.archivedCount = archivedCount;
        this.avgProgress = avgProgress != null ? avgProgress : BigDecimal.ZERO;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(long activeCount) {
        this.activeCount = activeCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public long getArchivedCount() {
        return archivedCount;
    }

    public void setArchivedCount(long archivedCount) {
        this.archivedCount = archivedCount;
    }

    public BigDecimal getAvgProgress() {
        return avgProgress;
    }

    public void setAvgProgress(BigDecimal avgProgress) {
        this.avgProgress = avgProgress;
    }
}

