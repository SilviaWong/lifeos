package com.lifeos.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 健康指标记录展示视图对象
 */
@Schema(description = "健康指标记录详情")
public class HealthRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记录 ID", example = "1001")
    private Long id;

    @Schema(description = "所属用户 ID", example = "1")
    private Long userId;

    @Schema(description = "指标类型编码", example = "STEPS")
    private String metricCode;

    @Schema(description = "指标类型中文名称", example = "步数")
    private String metricName;

    @Schema(description = "测量数值", example = "8500")
    private BigDecimal metricValue;

    @Schema(description = "测量单位", example = "步")
    private String unit;

    @Schema(description = "记录发生时间", example = "2026-11-01 12:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;

    @Schema(description = "备注或饮食说明", example = "慢跑")
    private String note;

    @Schema(description = "数据来源", example = "MANUAL")
    private String source;

    @Schema(description = "生活参考标准评估结果：NORMAL(参考区间内), BELOW(低于参考值), ABOVE(高于参考值), UNKNOWN(未设定)", example = "NORMAL")
    private String standardStatus;

    @Schema(description = "标准评估文案提示（明确非医疗诊断）", example = "生活参考区间内（非医疗诊断）")
    private String standardHint;

    @Schema(description = "创建时间", example = "2026-11-01 12:30:05")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public HealthRecordVO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMetricCode() {
        return metricCode;
    }

    public void setMetricCode(String metricCode) {
        this.metricCode = metricCode;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public BigDecimal getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(BigDecimal metricValue) {
        this.metricValue = metricValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStandardStatus() {
        return standardStatus;
    }

    public void setStandardStatus(String standardStatus) {
        this.standardStatus = standardStatus;
    }

    public String getStandardHint() {
        return standardHint;
    }

    public void setStandardHint(String standardHint) {
        this.standardHint = standardHint;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

