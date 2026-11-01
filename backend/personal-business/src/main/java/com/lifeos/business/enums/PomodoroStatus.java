package com.lifeos.business.enums;

/**
 * 番茄钟会话状态
 */
public enum PomodoroStatus {

    /**
     * 进行中
     */
    RUNNING("RUNNING", "进行中"),

    /**
     * 已暂停
     */
    PAUSED("PAUSED", "已暂停"),

    /**
     * 正常完成
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 中途放弃/中止
     */
    ABORTED("ABORTED", "已中止");

    private final String code;
    private final String desc;

    PomodoroStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PomodoroStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PomodoroStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code.trim())) {
                return status;
            }
        }
        return null;
    }

    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == ABORTED;
    }
}

