package com.lifeos.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 健康指标趋势数据单点
 */
@Schema(description = "健康趋势聚合数据点")
public class HealthTrendPointVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "聚合日期 (yyyy-MM-dd)", example = "2026-11-01")
    private String date;

    @Schema(description = "聚合数值（步数/饮水等为当日累计总和，体重为最新值）", example = "8520")
    private BigDecimal value;

    @Schema(description = "当日打卡/记录笔数", example = "2")
    private int recordCount;

    public HealthTrendPointVO() {
    }

    public HealthTrendPointVO(String date, BigDecimal value, int recordCount) {
        this.date = date;
        this.value = value;
        this.recordCount = recordCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }
}

