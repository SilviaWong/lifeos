package com.lifeos.statistics.vo;

import java.io.Serializable;

/**
 * 健康数据记录类型分布统计 VO
 */
public class HealthTypeStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 健康记录类型枚举值（STEPS, SLEEP, WEIGHT, WATER, DIET）
     */
    private String recordType;

    /**
     * 健康记录类型中文名（步数、睡眠、体重、饮水、饮食）
     */
    private String recordTypeName;

    /**
     * 记录条数
     */
    private Long count = 0L;

    public HealthTypeStatVO() {
    }

    public HealthTypeStatVO(String recordType, String recordTypeName, Long count) {
        this.recordType = recordType;
        this.recordTypeName = recordTypeName;
        this.count = count != null ? count : 0L;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getRecordTypeName() {
        return recordTypeName;
    }

    public void setRecordTypeName(String recordTypeName) {
        this.recordTypeName = recordTypeName;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}

