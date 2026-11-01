package com.lifeos.business.enums;

/**
 * 待办任务优先级枚举
 */
public enum TodoPriority {

    LOW(1, "低"),
    MEDIUM(2, "中"),
    HIGH(3, "高"),
    URGENT(4, "紧急");

    private final int value;
    private final String description;

    TodoPriority(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static String getDescriptionByValue(Integer value) {
        if (value == null) {
            return MEDIUM.getDescription();
        }
        for (TodoPriority priority : values()) {
            if (priority.getValue() == value) {
                return priority.getDescription();
            }
        }
        return MEDIUM.getDescription();
    }

    public static boolean isValid(Integer value) {
        if (value == null) {
            return false;
        }
        for (TodoPriority priority : values()) {
            if (priority.getValue() == value) {
                return true;
            }
        }
        return false;
    }
}

