package com.lifeos.business.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康指标参考标准配置实体
 */
@TableName("health_standard")
public class HealthStandard implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 指标编码，如 STEPS, SLEEP_HOURS, WEIGHT, WATER_ML, CALORIES
     */
    private String metricCode;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 测量单位
     */
    private String unit;

    /**
     * 推荐最小参考值（可空）
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private BigDecimal minValue;

    /**
     * 推荐最大参考值（可空）
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private BigDecimal maxValue;

    /**
     * 适用群体范围：GENERAL, MALE, FEMALE
     */
    private String applicableScope;

    /**
     * 说明与非医疗免责提示
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String description;

    /**
     * 生效起始日期
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDate effectiveFrom;

    /**
     * 生效截止日期
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDate effectiveTo;

    /**
     * 启用状态：1-启用, 0-禁用
     */
    private Integer status;

    /**
     * 创建管理员 ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    public HealthStandard() {
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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

