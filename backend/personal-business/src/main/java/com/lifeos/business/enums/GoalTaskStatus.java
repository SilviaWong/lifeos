package com.lifeos.business.enums;

/**
 * 目标子任务状态枚举
 */
public enum GoalTaskStatus {

    /**
     * 待办
     */
    TODO("TODO", "待办"),

    /**
     * 已完成
     */
    DONE("DONE", "已完成");

    private final String code;
    private final String description;

    GoalTaskStatus(String code, String description) {
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
        for (GoalTaskStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code.trim())) {
                return true;
            }
        }
        return false;
    }

    public static GoalTaskStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (GoalTaskStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code.trim())) {
                return status;
            }
        }
        return null;
    }
}

