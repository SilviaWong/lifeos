package com.lifeos.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 健康指标趋势查询参数
 */
@Schema(description = "健康指标趋势查询参数")
public class HealthTrendQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "指标类型编码：STEPS, SLEEP_HOURS, WEIGHT, WATER_ML, CALORIES", requiredMode = Schema.RequiredMode.REQUIRED, example = "STEPS")
    @NotBlank(message = "指标类型不能为空")
    private String metricCode;

    @Schema(description = "起始日期 (yyyy-MM-dd)，默认最近 7 天", example = "2026-10-26")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "截止日期 (yyyy-MM-dd)，默认今天", example = "2026-11-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "聚合周期维度：DAY(日聚合), WEEK(周聚合), MONTH(月聚合)", example = "DAY")
    private String period = "DAY";

    public HealthTrendQueryRequest() {
    }

    public String getMetricCode() {
        return metricCode;
    }

    public void setMetricCode(String metricCode) {
        this.metricCode = metricCode;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getPeriod() {
        return period != null ? period : "DAY";
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}

