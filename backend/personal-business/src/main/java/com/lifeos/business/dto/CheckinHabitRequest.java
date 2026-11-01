package com.lifeos.business.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 习惯打卡请求参数
 */
public class CheckinHabitRequest {

    /**
     * 打卡日期（默认当天，禁止未来日期）
     */
    private LocalDate checkinDate;

    @Size(max = 500, message = "打卡心得备注不能超过500个字符")
    private String note;

    public CheckinHabitRequest() {
    }

    public CheckinHabitRequest(LocalDate checkinDate, String note) {
        this.checkinDate = checkinDate;
        this.note = note;
    }

    public LocalDate getCheckinDate() {
        return checkinDate;
    }

    public void setCheckinDate(LocalDate checkinDate) {
        this.checkinDate = checkinDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

