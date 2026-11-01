package com.lifeos.business.enums;

/**
 * 目标状态枚举
 */
public enum GoalStatus {

    /**
     * 未开始
     */
    NOT_STARTED("NOT_STARTED", "未开始"),

    /**
     * 进行中
     */
    IN_PROGRESS("IN_PROGRESS", "进行中"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 已归档
     */
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String description;

    GoalStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        for (GoalStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code.trim())) {
                return true;
            }
        }
        return false;
    }

    public static GoalStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (GoalStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code.trim())) {
                return status;
            }
        }
        return null;
    }
}

