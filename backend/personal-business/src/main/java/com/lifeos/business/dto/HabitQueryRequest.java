package com.lifeos.business.dto;

/**
 * 习惯查询筛选参数
 */
public class HabitQueryRequest {

    private String status;

    private String frequency;

    private String keyword;

    public HabitQueryRequest() {
    }

    public HabitQueryRequest(String status, String frequency, String keyword) {
        this.status = status;
        this.frequency = frequency;
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}

