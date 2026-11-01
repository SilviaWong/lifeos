package com.lifeos.statistics.vo;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 统计查询日期范围信息 VO
 */
public class DateRangeFilterVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate startDate;
    private LocalDate endDate;
    private Long dayCount;

    public DateRangeFilterVO() {
    }

    public DateRangeFilterVO(LocalDate startDate, LocalDate endDate, Long dayCount) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.dayCount = dayCount;
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

    public Long getDayCount() {
        return dayCount;
    }

    public void setDayCount(Long dayCount) {
        this.dayCount = dayCount;
    }
}

