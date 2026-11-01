package com.lifeos.business.enums;

/**
 * 番茄钟会话结束原因
 */
public enum PomodoroEndReason {

    /**
     * 正常计时完成
     */
    NORMAL_FINISH("NORMAL_FINISH", "正常完成"),

    /**
     * 用户主动中止/放弃
     */
    USER_ABORT("USER_ABORT", "主动放弃"),

    /**
     * 系统超时自动收尾
     */
    TIMEOUT("TIMEOUT", "超时结束");

    private final String code;
    private final String desc;

    PomodoroEndReason(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

