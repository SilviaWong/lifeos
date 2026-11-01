package com.lifeos.statistics.vo;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户趋势数据点 VO（每日新增注册用户与活跃用户）
 */
public class UserTrendPointVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 当日新增注册用户数
     */
    private Long newUsers = 0L;

    /**
     * 当日活跃用户数 (DAU)
     */
    private Long activeUsers = 0L;

    public UserTrendPointVO() {
    }

    public UserTrendPointVO(LocalDate date, Long newUsers, Long activeUsers) {
        this.date = date;
        this.newUsers = newUsers != null ? newUsers : 0L;
        this.activeUsers = activeUsers != null ? activeUsers : 0L;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getNewUsers() {
        return newUsers;
    }

    public void setNewUsers(Long newUsers) {
        this.newUsers = newUsers;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }
}

