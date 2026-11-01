package com.lifeos.business.enums;

/**
 * 番茄钟会话类型
 */
public enum PomodoroSessionType {

    /**
     * 专注会话
     */
    FOCUS("FOCUS", "专注"),

    /**
     * 短休息
     */
    SHORT_BREAK("SHORT_BREAK", "短休息"),

    /**
     * 长休息
     */
    LONG_BREAK("LONG_BREAK", "长休息");

    private final String code;
    private final String desc;

    PomodoroSessionType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PomodoroSessionType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PomodoroSessionType type : values()) {
            if (type.getCode().equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }

    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}

