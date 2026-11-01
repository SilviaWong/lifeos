package com.lifeos.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lifeos.common.api.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 健康指标记录分页筛选请求参数
 */
@Schema(description = "健康指标记录分页筛选请求参数")
public class HealthRecordQueryRequest extends PageQuery {

    private static final long serialVersionUID = 1L;

    @Schema(description = "按指标类型筛选：STEPS, SLEEP_HOURS, WEIGHT, WATER_ML, CALORIES", example = "STEPS")
    private String metricCode;

    @Schema(description = "起始日期 (yyyy-MM-dd)", example = "2026-10-25")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "截止日期 (yyyy-MM-dd)", example = "2026-11-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    public HealthRecordQueryRequest() {
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
}

