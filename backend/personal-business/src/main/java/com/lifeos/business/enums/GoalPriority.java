package com.lifeos.business.enums;

/**
 * 目标优先级枚举 (1-高, 2-中, 3-低)
 */
public enum GoalPriority {

    HIGH(1, "高"),
    MEDIUM(2, "中"),
    LOW(3, "低");

    private final int value;
    private final String description;

    GoalPriority(int value, String description) {
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
        for (GoalPriority priority : values()) {
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
        for (GoalPriority priority : values()) {
            if (priority.getValue() == value) {
                return true;
            }
        }
        return false;
    }
}

