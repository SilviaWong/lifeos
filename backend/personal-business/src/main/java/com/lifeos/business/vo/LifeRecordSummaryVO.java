package com.lifeos.business.vo;

import java.io.Serializable;

/**
 * 生活记录概览统计 VO
 */
public class LifeRecordSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 物品总数
     */
    private Long totalItemCount = 0L;

    /**
     * 当前借出中物品数
     */
    private Long lentItemCount = 0L;

    /**
     * 借出逾期未还数
     */
    private Long overdueLendingCount = 0L;

    /**
     * 备忘录总数
     */
    private Long totalMemoCount = 0L;

    /**
     * 置顶备忘录数
     */
    private Long pinnedMemoCount = 0L;

    /**
     * 累计日记总数
     */
    private Long totalDiaryCount = 0L;

    /**
     * 本月已写日记篇数
     */
    private Long thisMonthDiaryCount = 0L;

    public LifeRecordSummaryVO() {
    }

    public Long getTotalItemCount() {
        return totalItemCount;
    }

    public void setTotalItemCount(Long totalItemCount) {
        this.totalItemCount = totalItemCount;
    }

    public Long getLentItemCount() {
        return lentItemCount;
    }

    public void setLentItemCount(Long lentItemCount) {
        this.lentItemCount = lentItemCount;
    }

    public Long getOverdueLendingCount() {
        return overdueLendingCount;
    }

    public void setOverdueLendingCount(Long overdueLendingCount) {
        this.overdueLendingCount = overdueLendingCount;
    }

    public Long getTotalMemoCount() {
        return totalMemoCount;
    }

    public void setTotalMemoCount(Long totalMemoCount) {
        this.totalMemoCount = totalMemoCount;
    }

    public Long getPinnedMemoCount() {
        return pinnedMemoCount;
    }

    public void setPinnedMemoCount(Long pinnedMemoCount) {
        this.pinnedMemoCount = pinnedMemoCount;
    }

    public Long getTotalDiaryCount() {
        return totalDiaryCount;
    }

    public void setTotalDiaryCount(Long totalDiaryCount) {
        this.totalDiaryCount = totalDiaryCount;
    }

    public Long getThisMonthDiaryCount() {
        return thisMonthDiaryCount;
    }

    public void setThisMonthDiaryCount(Long thisMonthDiaryCount) {
        this.thisMonthDiaryCount = thisMonthDiaryCount;
    }
}

