package com.lifeos.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 管理端健康指标参考标准配置传输对象
 */
@Schema(description = "管理端健康指标参考标准配置")
public class HealthStandardDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "指标编码：STEPS, SLEEP_HOURS, WEIGHT, WATER_ML, CALORIES", requiredMode = Schema.RequiredMode.REQUIRED, example = "STEPS")
    @NotBlank(message = "指标编码不能为空")
    private String metricCode;

    @Schema(description = "指标名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "每日步数")
    @NotBlank(message = "指标名称不能为空")
    @Size(max = 64, message = "指标名称长度不能超过 64 个字符")
    private String metricName;

    @Schema(description = "测量单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "步")
    @NotBlank(message = "单位不能为空")
    private String unit;

    @Schema(description = "推荐参考最小值", example = "6000")
    private BigDecimal minValue;

    @Schema(description = "推荐参考最大值", example = "12000")
    private BigDecimal maxValue;

    @Schema(description = "适用范围：GENERAL, MALE, FEMALE", example = "GENERAL")
    private String applicableScope = "GENERAL";

    @Schema(description = "生活指引描述与免责提示", example = "推荐每日步数参考区间，不作为医疗诊断依据")
    @Size(max = 500, message = "描述长度不能超过 500 个字符")
    private String description;

    @Schema(description = "生效起始日期 (yyyy-MM-dd)", example = "2026-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveFrom;

    @Schema(description = "生效截止日期 (yyyy-MM-dd)", example = "2099-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveTo;

    @Schema(description = "启用状态：1-启用, 0-禁用", example = "1")
    private Integer status = 1;

    public HealthStandardDTO() {
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
        return applicableScope != null ? applicableScope : "GENERAL";
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
        return status != null ? status : 1;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

