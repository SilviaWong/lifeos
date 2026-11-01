package com.lifeos.business.enums;

import java.math.BigDecimal;
import java.util.List;

/**
 * 健康指标类型枚举定义与范围校验
 */
public enum HealthMetricType {

    /**
     * 每日步数
     */
    STEPS("STEPS", "步数", "步", List.of("步", "count"),
            BigDecimal.ZERO, BigDecimal.valueOf(200000), "SUM"),

    /**
     * 睡眠时长（小时）
     */
    SLEEP_HOURS("SLEEP_HOURS", "睡眠时长", "小时", List.of("小时", "h", "hour"),
            BigDecimal.ZERO, BigDecimal.valueOf(24), "SUM"),

    /**
     * 体重（公斤）
     */
    WEIGHT("WEIGHT", "体重", "kg", List.of("kg", "公斤"),
            BigDecimal.valueOf(10), BigDecimal.valueOf(400), "LATEST"),

    /**
     * 饮水量（毫升）
     */
    WATER_ML("WATER_ML", "饮水量", "ml", List.of("ml", "毫升", "mL"),
            BigDecimal.ZERO, BigDecimal.valueOf(20000), "SUM"),

    /**
     * 饮食摄入能量（千卡）
     */
    CALORIES("CALORIES", "饮食摄入", "kcal", List.of("kcal", "千卡", "大卡"),
            BigDecimal.ZERO, BigDecimal.valueOf(20000), "SUM");

    private final String code;
    private final String name;
    private final String defaultUnit;
    private final List<String> supportedUnits;
    private final BigDecimal minAllowed;
    private final BigDecimal maxAllowed;
    private final String aggregateMethod; // SUM or LATEST / AVG

    HealthMetricType(String code, String name, String defaultUnit, List<String> supportedUnits,
                     BigDecimal minAllowed, BigDecimal maxAllowed, String aggregateMethod) {
        this.code = code;
        this.name = name;
        this.defaultUnit = defaultUnit;
        this.supportedUnits = supportedUnits;
        this.minAllowed = minAllowed;
        this.maxAllowed = maxAllowed;
        this.aggregateMethod = aggregateMethod;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    public List<String> getSupportedUnits() {
        return supportedUnits;
    }

    public BigDecimal getMinAllowed() {
        return minAllowed;
    }

    public BigDecimal getMaxAllowed() {
        return maxAllowed;
    }

    public String getAggregateMethod() {
        return aggregateMethod;
    }

    public static HealthMetricType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (HealthMetricType type : values()) {
            if (type.getCode().equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }

    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public boolean isUnitSupported(String unit) {
        if (unit == null || unit.isBlank()) {
            return true;
        }
        return supportedUnits.stream().anyMatch(u -> u.equalsIgnoreCase(unit.trim()));
    }

    public boolean isValueInRange(BigDecimal value) {
        if (value == null) {
            return false;
        }
        return value.compareTo(minAllowed) >= 0 && value.compareTo(maxAllowed) <= 0;
    }
}

