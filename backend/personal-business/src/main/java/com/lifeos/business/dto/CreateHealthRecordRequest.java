package com.lifeos.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 录入健康指标请求参数
 */
@Schema(description = "录入健康指标请求参数")
public class CreateHealthRecordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "指标类型编码：STEPS, SLEEP_HOURS, WEIGHT, WATER_ML, CALORIES", requiredMode = Schema.RequiredMode.REQUIRED, example = "STEPS")
    @NotBlank(message = "指标类型不能为空")
    private String metricCode;

    @Schema(description = "测量数值", requiredMode = Schema.RequiredMode.REQUIRED, example = "8500")
    @NotNull(message = "测量数值不能为空")
    @DecimalMin(value = "0.0", message = "测量数值不能为负数")
    private BigDecimal metricValue;

    @Schema(description = "单位（如：步, 小时, kg, ml, kcal），为空时使用指标默认单位", example = "步")
    private String unit;

    @Schema(description = "记录时间（yyyy-MM-dd HH:mm:ss），为空时取当前时间", example = "2026-11-01 12:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;

    @Schema(description = "备注或饮食记录详情", example = "晚间慢跑 4 公里")
    @Size(max = 500, message = "备注长度不得超过 500 个字符")
    private String note;

    @Schema(description = "数据来源：MANUAL, DEVICE, IMPORT", example = "MANUAL")
    private String source = "MANUAL";

    public CreateHealthRecordRequest() {
    }

    public String getMetricCode() {
        return metricCode;
    }

    public void setMetricCode(String metricCode) {
        this.metricCode = metricCode;
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
        return source != null ? source : "MANUAL";
    }

    public void setSource(String source) {
        this.source = source;
    }
}

