package com.lifeos.business.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 个人日记列表筛选请求 DTO
 */
public class DiaryQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String mood;

    private String keyword;

    private Integer current = 1;
    private Integer size = 10;

    public DiaryQueryRequest() {
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

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getCurrent() {
        return current != null && current > 0 ? current : 1;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getSize() {
        if (size == null || size <= 0) return 10;
        return Math.min(size, 100);
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}

