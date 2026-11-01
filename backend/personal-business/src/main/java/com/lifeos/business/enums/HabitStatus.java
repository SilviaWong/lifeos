package com.lifeos.business.enums;

/**
 * 习惯状态枚举
 */
public enum HabitStatus {

    ACTIVE("ACTIVE", "进行中"),
    PAUSED("PAUSED", "已暂停"),
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String desc;

    HabitStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static HabitStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (HabitStatus item : values()) {
            if (item.code.equalsIgnoreCase(code.trim())) {
                return item;
            }
        }
        return null;
    }
}

