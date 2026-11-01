package com.lifeos.business.enums;

/**
 * 待办任务状态枚举
 */
public enum TodoStatus {

    /**
     * 待办中
     */
    TODO("TODO", "待办"),

    /**
     * 进行中
     */
    IN_PROGRESS("IN_PROGRESS", "进行中"),

    /**
     * 已完成
     */
    DONE("DONE", "已完成"),

    /**
     * 已取消
     */
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;

    TodoStatus(String code, String description) {
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
        for (TodoStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code.trim())) {
                return true;
            }
        }
        return false;
    }

    public static TodoStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (TodoStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code.trim())) {
                return status;
            }
        }
        return null;
    }
}

