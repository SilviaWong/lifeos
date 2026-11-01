package com.lifeos.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康指标参考标准视图对象
 */
@Schema(description = "健康指标参考标准详情")
public class HealthStandardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标准 ID", example = "1")
    private Long id;

    @Schema(description = "指标编码", example = "STEPS")
    private String metricCode;

    @Schema(description = "指标名称", example = "每日步数")
    private String metricName;

    @Schema(description = "计量单位", example = "步")
    private String unit;

    @Schema(description = "推荐参考最小值", example = "6000")
    private BigDecimal minValue;

    @Schema(description = "推荐参考最大值", example = "12000")
    private BigDecimal maxValue;

    @Schema(description = "适用范围", example = "GENERAL")
    private String applicableScope;

    @Schema(description = "标准说明与非医疗免责提示", example = "成年人每日推荐步数参考，不构成医疗诊断建议")
    private String description;

    @Schema(description = "生效起始日期", example = "2026-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveFrom;

    @Schema(description = "生效截止日期", example = "2099-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveTo;

    @Schema(description = "启用状态：1-启用, 0-禁用", example = "1")
    private Integer status;

    @Schema(description = "更新时间", example = "2026-11-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public HealthStandardVO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public String getApplicableScope() {
        return applicableScope;
    }

    public void setApplicableScope(String applicableScope) {
        this.applicableScope = applicableScope;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

