package com.lifeos.business.enums;

/**
 * 习惯频率枚举
 */
public enum HabitFrequency {

    DAILY("DAILY", "每日"),
    WEEKLY("WEEKLY", "每周");

    private final String code;
    private final String desc;

    HabitFrequency(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static HabitFrequency fromCode(String code) {
        if (code == null) {
            return DAILY;
        }
        for (HabitFrequency item : values()) {
            if (item.code.equalsIgnoreCase(code.trim())) {
                return item;
            }
        }
        return null;
    }
}

