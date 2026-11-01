package com.lifeos.business.enums;

/**
 * 收支类型枚举
 */
public enum FinanceRecordType {

    INCOME("INCOME", "收入"),
    EXPENSE("EXPENSE", "支出");

    private final String code;
    private final String desc;

    FinanceRecordType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static FinanceRecordType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (FinanceRecordType item : values()) {
            if (item.code.equalsIgnoreCase(code.trim())) {
                return item;
            }
        }
        return null;
    }
}

